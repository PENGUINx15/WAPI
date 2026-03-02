# Глубокий архитектурный аудит WAPI (Paper plugin + custom command framework)

## 0) Диагноз по факту

Это не «готовый command framework», а полу-собранный каркас с красивой модульной раскладкой и критически недореализованным runtime. Публичная архитектурная декларация (`ARCHITECTURE_V2.md`) обещает зрелую pipeline-модель, но реальный код содержит пустые стадии, отсутствие bridge к Bukkit command map и ряд unsafe-практик в конкуренции/IO.

---

## 1) A) Layer separation: где слои, а где имитация

### 1.1 Разделение модулей есть, но runtime-композиции нет

Да, есть отдельные модули `command-annotations`, `command-core`, `command-paper-adapter`, `WAPI`. Но это структурная раскладка исходников, а не завершённая архитектура выполнения:

- `WAPI` создаёт `CommandFrameworkBootstrap`, регистрирует объект команды и на этом всё. Нигде не связывается с `PluginCommand#setExecutor` / `setTabCompleter`. Следовательно, pipeline не получает управление от Bukkit вовсе. `framework` фактически не участвует в обработке `/main` в проде.
- `CommandFrameworkBootstrap#register` каждый раз только rebuild-ит дерево и выкидывает результат. Дерево не сохраняется в состоянии bootstrap, не используется маршрутизацией.

Вердикт: слой интеграции дырявый, фреймворк не доведён до рабочей эксплуатационной цепочки.

### 1.2 Core не полностью изолирован от платформы

Формально core использует абстракции (`CommandSenderAdapter`, `PermissionEvaluator`, `Scheduler`, `Audience`). Это плюс. Но:

- Core не владеет жизненным циклом этих абстракций и не формирует orchestration-объект типа `CommandRuntime`.
- `PaperPermissionEvaluator` делает downcast к конкретному `PaperCommandSenderAdapter`, что ломает идею платформенной изоляции: abstraction leaking через adapter boundary.

### 1.3 God-class риск смещён в bootstrap/utility-слои

В текущей версии нет одного гигантского диспетчера, но есть противоположная проблема — «анемичная декомпозиция»: множество классов-обёрток по 5–10 строк без связующего поведения. Это не SRP-зрелость, это функциональная пустота. Слишком много «типов ради типов», слишком мало инвариантов.

---

## 2) B) Командный фреймворк: разбор по подсистемам

## 2.1 Dispatcher design: отсутствует

`CommandPipeline` умеет только прогонять список стадий и интерцепторов. Никакой интеграции с источником команд, никакой result-модели, никакого контракта async/sync execution, никакого cancellation/short-circuit API кроме исключений.

Стадии (`RoutingStage`, `ArgumentParsingStage`, `ValidationStage`, `AuthorizationStage`, `CooldownStage`, `InvocationStage`, `PostProcessingStage`, `ErrorHandlingStage`) полностью пустые — возвращают context как есть. Это не MVP, это заглушки.

Практически: у вас нет dispatcher-а как системы.

## 2.2 Command tree implementation

`CommandTree#route`:

- на каждом токене два последовательных stream-поиска по `children()` (literal, потом argument) — это лишняя аллокация/итерация;
- выбирает **первый** ARGUMENT-узел при отсутствии literal. При нескольких аргументных ветках на одном уровне — недетерминированность по insertion order, а не по правилу specificity;
- не возвращает трассировку совпавших аргументов (какой placeholder matched), значит аргументный parsing вынужден дублировать логику/контекст вне роутера.

Сама структура immutable после `build()`, это хорошо. Но runtime-алгоритм маршрутизации — наивный.

## 2.3 Metadata caching strategy

`CommandMetadataCache` кэширует скан класса через `computeIfAbsent` и отдельно bind-ит instance в `BoundCommandMethod`. Это лучше старого анти-паттерна «instance в кэше». Но проблем хватает:

- нет инвалидации/версирования для hot-reload сценариев;
- нет preheat API для массового скана при старте;
- не учитываются classloader lifecycle нюансы Paper-перезагрузок (потенциальное удержание ссылок до GC при внешних ссылках на cache owner).

## 2.4 Argument resolution system

`ResolverRegistry`:

- ключ по точному `Class<?>`, без assignable/primitive-wrapper/enum-strategy fallback;
- `best(type)` бросает `IllegalArgumentException` — инфраструктурный провал превращается в runtime exception без типизации;
- `register` synchronized + ConcurrentHashMap вместе — лишняя и неочевидная смесь стратегий;
- разрешение «лучшего» резолвера зависит только от integer priority, нет capability-check по контексту/аннотациям/source.

Для production framework это слабая модель extensibility.

## 2.5 Tab completion architecture

`CompletionEngine`:

- suggestion cache лежит в `CommandContext` как mutable `HashMap`, что ломает иммутабельную семантику record-контекста;
- кэш-key: `argument.name() + ":" + input` — коллизии между разными командами с одинаковым именем аргумента;
- отсутствует TTL/size-limit и политика invalidation;
- нет batching и нет cancelation при быстром наборе таба игроком;
- нет throttling от expensive resolver-ов.

## 2.6 Error propagation model

`CommandPipeline#execute` ловит `Throwable`, а не `Exception`. Это архитектурно токсично:

- глотает `OutOfMemoryError`, `StackOverflowError`, `LinkageError` и прочие JVM-level фаталы;
- маскирует критические сбои как «обычную ошибку команды» через `ErrorPresenter`.

`DefaultErrorPresenter` маппит только `UserInputException`/`AuthorizationException`; всё остальное превращает в «internal error» без логирования stack trace. Дебаг продовых аварий будет мучительным.

## 2.7 Validation system

`ValidationService` жёстко инициализирует встроенные валидаторы в конструкторе, нет модульной композиции/пакетной регистрации/конфликт-политики.

- unchecked cast в `validate`;
- валидация зависит от runtime annotation list из reflection без compile-time model;
- нет cross-argument validation;
- нет validation groups/conditional constraints;
- нет локализации сообщений.

---

## 3) C) Архитектурные smells

## 3.1 Mixed concerns

`MessageManager` — статический God-utility:

- placeholder интеграция;
- парсинг MiniMessage и fallback в legacy;
- транспорт (chat/actionbar/title/subtitle/json);
- шаблонизатор;
- логгер.

Это пять разных ответственностей в одном классе со статикой.

## 3.2 Hidden state

`CommandContext` выглядит как immutable record, но содержит mutable `suggestionCache` map, мутируемый из `CompletionEngine`. Это скрытое мутабельное состояние внутри supposedly immutable value-object.

## 3.3 Mutable global-ish state

- `CooldownManager` держит lifecycle собственного `ScheduledExecutorService`, не привязанного к lifecycle плагина (`shutdown` отсутствует).
- `SQLiteManager` хранит shared `Connection` и использует его из `CompletableFuture.supplyAsync` без выделенного executor-а и без пула соединений.

## 3.4 Reflection overuse / reflection without contract hardening

`AnnotationCommandScanner`:

- `method.setAccessible(true)` на все subcommand-методы;
- разбор аргументов только по `@Arg`, параметры без аннотации silently пропускаются;
- нет валидации сигнатур методов команды на этапе скана (например, sender-first контракт, duplicate names, unsupported types).

Итог: ошибки конфигурации ловятся поздно и случайно.

## 3.5 Leaky abstractions

`PaperPermissionEvaluator` кастит интерфейсный sender к конкретному типу адаптера. Если в core попадёт другой адаптер (тестовый, прокси, future-bridge), получите `ClassCastException`.

## 3.6 Poor boundaries

В `WAPI` нет composition root уровня «порт/адаптер binding». Есть только `new CommandFrameworkBootstrap()` и `register`. Нет deterministic wiring платформенных сервисов (logger/scheduler/permission/audience/resolvers/validators).

---

## 4) D) Performance audit

## 4.1 Reflection usage frequency

Сканер выполняет reflection на регистрацию команд. Это приемлемо, если разово на startup. Но текущий bootstrap rebuild-ит tree после каждой регистрации, а tree нигде не кэшируется для runtime использования — лишняя работа без эффекта.

## 4.2 Resolver instantiation / lookup

Registry хранит цепочки как immutable snapshots — это ок для lock-free reads. Но lookup по exact type приведёт к раздутию количества резолверов/дублированию логики (Integer/int, offline/online player, enum variants).

## 4.3 Tree traversal complexity

`route` формально O(depth * children_at_level) с линейным поиском по списку детей. Для 100+ команд с общими префиксами и ветвлением это станет заметным на горячем пути (особенно с частым tab completion).

## 4.4 Blocking operations on main thread

`SQLiteManager` только предупреждает (`warning`) при вызове с main thread, но **не блокирует** и не кидает исключение. То есть «защита» косметическая; кто угодно может выполнить тяжелый SQL прямо в тике.

Плюс, `CompletableFuture.supplyAsync` использует общий ForkJoinPool, что в plugin-среде нежелательно: вы конкурируете за системный пул с остальными задачами JVM.

## 4.5 SQLite safety

Один shared `Connection` используется потенциально из нескольких async задач одновременно. JDBC/SQLite threading semantics для одного connection в таком режиме ненадёжны; легко получить `database is locked`, race по statement lifecycle и непредсказуемые задержки.

---

## 5) E) Extensibility audit

## 5.1 Добавление новых argument types

Да, теоретически через `ResolverRegistry#register`. Практически больно:

- нет fallback chain по supertype/interface;
- нет scoped resolver-ов (per-command/per-annotation);
- нет parser-combinator для составных типов;
- нет unified conversion/validation pipeline.

## 5.2 Middleware introduction

`CommandInterceptor` существует, но pipeline не предоставляет контрактов short-circuit/abort/result override. Интерцептор может только преобразовать context before/after stage. Это слишком слабая точка расширения для настоящего middleware.

## 5.3 Async execution integration

Ни `CommandStage`, ни `CommandPipeline` не async-aware (`CommandContext execute(...)` вместо `CompletionStage<CommandContext>`). Вставка асинхронных стадий потребует ломающего API-редизайна.

## 5.4 Standalone extraction potential

Вынести как самостоятельный framework можно только после:

1) реализации реального runtime;
2) удаления platform downcast leak;
3) async-capable pipeline;
4) четкого SPI для adapter-ов.

Сейчас это «набор заготовок», не конкурентный framework artifact.

---

## 6) F) Приоритизированный roadmap

## 6.1 Immediate fixes (высокий эффект, низкий риск)

1. Убрать `catch(Throwable)` из pipeline. Ловить только `Exception`; fatal ошибки пробрасывать.
2. Сделать `CommandContext` действительно immutable: вынести suggestion cache наружу (например, в request-scoped `ExecutionState`).
3. Переписать `PaperPermissionEvaluator` без downcast (добавить метод permission-check в `CommandSenderAdapter` или отдельный capability).
4. В `SQLiteManager`:
   - выделить собственный `ExecutorService`;
   - запретить main-thread SQL жёстко (exception);
   - сериализовать доступ к SQLite (single-thread executor) или перейти на connection-per-operation + пул.
5. Добавить shutdown hooks для `CooldownManager` executor и DB manager.

## 6.2 Medium-term refactors

1. Реализовать стадии pipeline по-настоящему, включая route->parse->validate->authorize->invoke.
2. Ввести `CommandRuntime` (tree + pipeline + resolver registry + validation + error presenter) как единый orchestrator.
3. Изменить `CommandTree` на map-based children lookup (`literalChildren` map + отдельная коллекция argument nodes с priority rules).
4. Сделать typed error hierarchy + обязательное логирование stack trace на infrastructural errors.
5. Ввести сигнатурную валидацию команд на этапе scan/register (fail-fast startup).

## 6.3 Major redesign

1. Async-first pipeline (`CompletionStage<CommandResult>`) + планировщик bridge на платформу.
2. Middleware API уровня Cloud/ACF:
   - pre-routing
   - pre-parsing
   - pre-invocation
   - post-invocation
   - on-error
   - on-completion
3. Condition system (permission, sender type, world, cooldown, custom predicates) как расширяемые policy-объекты.
4. Выделить `command-bukkit-runtime` adapter со строгим binding к Bukkit command map и lifecycle.

---

## 7) G) Конкретные refactor-стратегии

## 7.1 Ввести полноценный CommandContext + ExecutionState

Разделить:

- `CommandContext` (immutable, только вход + metadata + parsed args);
- `ExecutionState` (mutable request-scope: caches, timings, diagnostics, middleware bag).

Это уберёт скрытые мутации и облегчит трассировку.

## 7.2 Ввести ExecutionPipeline v2

```java
interface CommandStage {
  CompletionStage<StageResult> execute(CommandContext ctx, ExecutionState state);
}

sealed interface StageResult {
  record Continue(CommandContext ctx) implements StageResult {}
  record Stop(CommandResult result) implements StageResult {}
}
```

Такой контракт решает short-circuit, async и контролируемое завершение.

## 7.3 Разрезать core и Bukkit adapter жёстче

- В core никаких downcast/знаний о Paper;
- В адаптере — реализация `PlatformCommandBinder`, превращающая Bukkit events/commands в core runtime calls;
- Протокол взаимодействия: только интерфейсы SPI.

## 7.4 Middleware layer

Вместо «списка интерцепторов before/after stage» сделать middleware chain уровня request lifecycle с возможностью:

- замерять latency;
- отменять выполнение;
- подменять response;
- добавлять audit trail и correlation IDs.

## 7.5 Улучшение dependency inversion

`CommandFrameworkBootstrap` должен принимать зависимости извне (DI-friendly):

- `ResolverRegistry`
- `ValidationService`
- `ErrorPresenter`
- `Scheduler`
- `PermissionEvaluator`
- `Audience`

Иначе вы зацементировали «один bootstrap, одна конфигурация, ноль тестируемости».

---

## 8) H) Архитектурная зрелость: 3.9 / 10

Оценка не 1/10 только потому, что:

- уже есть модульная декомпозиция;
- есть typed metadata model;
- есть попытка pipeline/interceptor дизайна;
- есть базовые платформенные абстракции.

Но до production-grade framework уровня ACF/Cloud далеко из-за ключевых провалов: пустой runtime, дырявый integration layer, слабая модель extensibility, async-неготовность, и небезопасный persistence/threads подход.

---

## 9) Сравнение с ACF / Cloud Command Framework

На текущем состоянии конкуренции нет вообще.

Что мешает дотянуться до их уровня:

1. Нет зрелого runtime (фактически нет исполнения).
2. Нет rich command model (conditions, contexts, parsers, suggestions contracts).
3. Нет battle-tested error/messaging/i18n системы.
4. Нет полноценного async execution и thread-boundary guarantees.
5. Нет экосистемы расширений и стабильного SPI.
6. Нет production telemetry hooks (metrics, tracing, structured logs).

Что нужно сделать, чтобы приблизиться:

1. Довести runtime до functional completeness (dispatcher + binder + completion).
2. Сделать API стабильным и расширяемым (middleware, conditions, parser pipeline).
3. Перевести инфраструктуру на async-safe модель с platform scheduler contract.
4. Ввести compatibility/performance test matrix (100+ commands, 50+ players simulated).
5. Поднять quality gate: mutation/unit/integration tests, static analysis, benchmark suite.

---

## 10) Что сломается под нагрузкой (100+ команд, 50+ игроков)

1. Таб-комплит начнёт деградировать из-за наивного дерева и бедного кэш-ключа suggestions.
2. Ошибки инфраструктуры будут теряться в generic «internal error», эксплуатация станет реактивным firefighting.
3. SQLite даст lock-contention и рандомные задержки из-за shared connection + unbounded common pool.
4. Lifecycle потечёт: scheduler у cooldown manager не закрывается, фоновые потоки переживут отключение плагина.
5. Любая попытка добавить async-стадии команд приведёт к каскадному API-сломy из-за синхронного контракта pipeline.

Это не гипотетика, это ожидаемое поведение текущего кода.
