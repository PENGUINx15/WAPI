# Жёсткий архитектурный аудит WAPI command framework (Paper)

## 1) Слои и границы (A)

### 1.1 Изоляция core от Bukkit: частичная и протекающая

- Формально есть `command-core` и `command-paper-adapter`, но граница дырявая: `PaperPermissionEvaluator` делает принудительный downcast к `PaperCommandSenderAdapter`, что убивает portability и нарушает DIP. Любой альтернативный adapter в рантайме сломается `ClassCastException`. (`PaperPermissionEvaluator#hasPermission`).
- `PaperCommandBinder` вручную собирает `services` map на каждый вызов команды, туда же создаёт `new PaperPermissionEvaluator()` per-invocation. Это не DI, а ad-hoc service locator. Нет lifecycle, нет singleton policy, нет явных зависимостей runtime. 
- `PlatformBridge` декларирует доставку suggestions, но `PaperPlatformBridge#deliverSuggestions` пустой no-op. Интерфейс есть, реального платформенного поведения нет.

Итог: core не изолирован, а замаскирован интерфейсами поверх hard-coded Paper-поведения.

### 1.2 Разделение ответственности: есть номинально, нет по потоку выполнения

- `CommandFrameworkBootstrap` одновременно: собирает runtime, регистрирует resolver-ы, создаёт scheduler, binder, error presenter, pipeline и сразу биндинг в Bukkit. Это composition root + wiring + feature toggles + platform bootstrap в одном классе.
- `PaperCommandBinder` одновременно executor-binding, request factory, сервисный locator и запуск async-сценария. Это явный mixed concerns.
- `CommandRegistrationService` хранит mutable список `bound`, сканирует и строит tree; никакой политики immutable snapshot для runtime нет.

### 1.3 God-class риск

- Гигантского God Object нет, но есть **God-bootstrap** анти-паттерн: всё знание о системе сведено в `CommandFrameworkBootstrap`; любое расширение (middleware, metrics, alt binder, alt permission strategy) требует правок в этом одном месте.

---

## 2) Командный фреймворк (B)

### 2.1 Dispatcher design

- Диспетчер размазан между `PaperCommandBinder` (вход), `CommandRuntime` (middleware + pipeline), `CommandPipeline` (stage chain). Это создаёт три места принятия решений и усложняет наблюдаемость.
- `PaperCommandBinder` не ждёт completion от `runtime.executeAndRespond(...)`, всегда возвращает `true`. Ошибки и задержки уезжают в фон без обратной связи в Bukkit.
- Нет back-pressure, timeout policy, cancellation token propagation в async-ветках.

### 2.2 CommandTree

- Маршрутизация делает literal lookup O(1), но аргументный fallback жёстко берёт `argumentChildren().get(0)`. Следствие: на глубине допускается только один аргументный edge, что срезает выразительность DSL.
- Builder искусственно запрещает больше одной аргументной ветки на depth (`CommandTreeBuildException`). Это не “безопасность”, это архитектурное ограничение масштаба.
- Optional аргументы вообще не участвуют в tree (`Builder#add` добавляет только required). Реальная маршрутизация optional вынесена в parsing stage через positional offset — хрупкая двухфазная логика.

### 2.3 Metadata cache

- `CommandMetadataCache` кэширует только по `Class<?>`; для plugin reload/нового classloader это безопасно лишь при корректном полном удалении ссылок на cache owner. Механизма очистки/invalidate нет.
- Нет precompute этапа для массового warmup, нет диагностики коллизий/невалидных сигнатур на старте.

### 2.4 ArgumentResolver system

- `ResolverRegistry#resolve` делает полный проход по всем зарегистрированным типам и сортировку кандидатов на каждый аргумент/каждый вызов. Это лишняя работа в горячем пути.
- При enum fallback создаётся новый `EnumArgumentResolver` каждый раз; нет кеша enum-resolver-ов.
- Алгоритм assignability двусторонний (`entryKey.isAssignableFrom(target) || target.isAssignableFrom(entryKey)`) — может включать лишние кандидаты и маскировать ошибки приоритетами.

### 2.5 Tab completion

- Engine есть (`CompletionEngine`), но не интегрирован в Paper binder: `setTabCompleter` отсутствует.
- `deliverSuggestions` не реализован.
- Cache в `ExecutionState` бесполезен для tab completion, потому что state живёт в рамках одного запроса.

### 2.6 Error propagation

- `CommandPipeline` оборачивает только `Exception` в `InfrastructureException`; `Error` не трогает (это хорошо).
- Но часть стадий кидает исключения синхронно, часть может возвращать failed stage async — нет единого нормализатора доменных ошибок на stage boundary.
- `DefaultErrorPresenter` жёстко кодирует locale (`Locale.ENGLISH` из runtime), игнорируя платформу/игрока.

### 2.7 Validation

- Валидация опирается только на runtime annotations и value object; нет compile-time/registration-time валидации сигнатур команд.
- unchecked cast в `ValidationService` (`Validator<Annotation>`) — технический долг, который скрывает ошибки registration.

---

## 3) Архитектурные запахи (C)

### 3.1 Mixed concerns

- `MessageManager` одновременно template-engine, MiniMessage/Legacy parser, Placeholder bridge, transport router (message/title/action/json) и logger helper.
- `SQLiteManager` смешивает thread policy, connection factory и data API.

### 3.2 Hidden state

- `ExecutionState.middlewareStorage` и `services` в `CommandContext` — не типобезопасные контейнеры, позволяющие скрытое договорное состояние между middleware/stage.

### 3.3 Mutable global-like state

- `CommandRegistrationService.bound` mutable и накапливается без механизма deregister/replace.
- `ResolverRegistry` mutable в рантайме; можно зарегистрировать resolver после старта без синхронизации с текущими активными execution policy.

### 3.4 Reflection overuse / unsafe reflection

- Reflection уместен на скане, но `method.setAccessible(true)` для каждого метода без необходимости в публичной API модели — дополнительный риск и обход инкапсуляции.
- Invocation в hot path через reflection без method-handle optimization.

### 3.5 Leaky abstractions / poor boundaries

- `CommandSenderAdapter` не содержит permission capability, поэтому permission вынесен во внешний evaluator, который потом ломает абстракцию downcast-ом.
- `PlatformBridge` формально двунаправленный, фактически только partial implementation.

---

## 4) Производительность и безопасность выполнения (D)

### 4.1 Reflection frequency

- Invocation каждого command handler через `Method.invoke` в `InvocationStage`; при 100+ командах и частых вызовах это measurable overhead.

### 4.2 Resolver lookup cost

- Каждому аргументу соответствует full scan реестра + сортировка кандидатов. С ростом количества resolver-ов latency растёт нелинейно на команду.

### 4.3 Tree traversal complexity

- Для literal edges эффективно, но невозможность иметь >1 argument edge ломает expressiveness и вынуждает дополнительные проверки на parsing/validation стадиях.

### 4.4 Main-thread blocking риски

- `PaperScheduler#runSync` используется в `deliverResult` всегда, даже если вызов уже на main thread; это создаёт лишние task hops.
- В плагине нет явного shutdown вызова для `PaperScheduler.shutdown()` и `SQLiteManager.shutdown()` в `onDisable`, следовательно утечки потоков/неконтролируемое завершение.

### 4.5 SQLite usage safety

- Переход на single-thread executor + connection-per-operation — хорошо для сериализации доступа, но:
  - нет транзакционного API,
  - нет retry/backoff на `database is locked`,
  - нет bounded queue и policy при перегрузке,
  - `ensureNotMainThread` проверяется до async dispatch, а не на уровне публичного контракта бизнес-слоя (легко обойти при рефакторинге).

---

## 5) SOLID нарушения (H + C)

- **S (SRP)**: `MessageManager`, `CommandFrameworkBootstrap`, `PaperCommandBinder` нарушают SRP.
- **O (OCP)**: для добавления middleware/новых стадий/другого binder нужно менять bootstrap-код, а не подключать модуль через конфигурацию.
- **L (LSP)**: `PermissionEvaluator` + downcast нарушают подстановку любых `CommandSenderAdapter`.
- **I (ISP)**: `PlatformBridge` перегружен обязанностями (result+suggestions+sender adaptation), при этом реализуется частично.
- **D (DIP)**: high-level runtime зависит от service locator map и конкретной сборки в bootstrap, а не от декларативного контейнера/фабрик.

---

## 6) Thread-safety проблемы (пункт 9)

- Нет гарантированного lifecycle shutdown для executor-ов (`PaperScheduler`, `CooldownManager`, `SQLiteManager`) на disable.
- `CooldownManager` в основном модуле имеет собственный scheduler thread и не реализует закрытие — утечка потоков на reload.
- Асинхронный command handler может менять Bukkit state из не-main thread; framework не предоставляет guardrail-ов/аннотаций thread affinity.

---

## 7) Extensibility ограничения (E)

### 7.1 Добавление argument types

- Возможно, но дороговато: нет resolver index cache, нет explicit ambiguity resolution report, нет declarative parser composition.

### 7.2 Middleware

- Базовый middleware chain есть, но не хватает контракта “around invocation + short-circuit reasons + structured error mapping + context mutation policy”. Сейчас middleware слишком low-level.

### 7.3 Async execution

- Pipeline async-capable по сигнатурам (`CompletionStage`), но execution policy не отделён от stage logic. Нет dispatcher strategy (main-thread stage / async stage / IO stage).

### 7.4 Выделение в standalone framework

Сейчас нельзя выпускать как конкурентный standalone:
- нет стабильного SPI для command binding и completion,
- нет testkit/contract tests для platform adapters,
- нет ABI-устойчивой модели ошибок/результатов,
- нет QoS-конфигурации (timeouts, executors, retries, limits).

---

## 8) Что сломается в проде (100+ команд, 50+ игроков)

1. Tab completion фактически отсутствует => UX деградирует сразу.
2. Resolver lookup overhead и reflection invoke начнут съедать latency на горячих командах.
3. Background executors без lifecycle management вызовут утечки после reload.
4. Ручной service map в binder приведёт к скрытым несовместимостям при добавлении новых сервисов.
5. Ограничение одного argument edge на depth начнёт ломать DSL при росте количества команд.
6. Отсутствие structured metrics (только `NoopMetricsSink`) лишит observability под нагрузкой.

---

## 9) Сравнение с ACF / Cloud Command Framework

Сейчас — не конкурент.

Почему:
- нет полноценного completion pipeline и platform-native интеграции;
- нет зрелой parser/typed context модели уровня Cloud;
- нет middleware/conditions/flags системы уровня ACF;
- нет DI/annotation processing/compile-time safety;
- нет battle-tested error model и rich help system.

Что нужно, чтобы приблизиться:
1. Вынести **command-core-engine** без Bukkit зависимостей вообще.
2. Ввести **typed `CommandContext` + argument cursor**, отказаться от Map-based parsedArguments.
3. Реализовать **ExecutionPipeline v2** с политиками thread affinity, timeout, cancellation, retries.
4. Добавить **Completion subsystem** как first-class citizen (AST-aware suggestions).
5. Ввести **Capability-based sender model** (permission/audience/player), убрать downcast.
6. Сделать **adapter contract tests** и property-based tests для router/parser.
7. Реализовать **observability**: metrics, tracing correlation, slow-command logs.

---

## 10) Приоритетный roadmap (F)

### Immediate (высокий эффект / низкий риск)

1. Убрать downcast из permission модели: перенести `hasPermission` в `CommandSenderAdapter` capability либо внедрить type-safe capability registry.
2. Реализовать tab completion end-to-end: binder `setTabCompleter` + `CompletionEngine` + `deliverSuggestions`.
3. Ввести lifecycle hooks в `WAPI#onDisable` для shutdown всех executor-ов.
4. Закрыть “service locator map” фабрикой `CommandRequestFactory`.
5. Кэшировать resolver match plan по `ArgumentMetadata`.

### Medium-term refactors

1. Разделить bootstrap на:
   - `FrameworkModule` (core wiring),
   - `PaperModule` (adapter wiring),
   - `PluginCompositionRoot`.
2. Ввести `CommandSignatureValidator` на регистрации (duplicate args, unsupported parameter layout, optional gaps).
3. Перевести invocation с reflection на cached MethodHandle.
4. Расширить `CommandResult` до structured payload (status code, user-safe message, developer details, metadata).

### Major redesign

1. Построить **Execution Pipeline 2.0**:
   - stage contract с execution mode (`SYNC_MAIN`, `ASYNC_CPU`, `ASYNC_IO`),
   - unified error envelope,
   - explicit short-circuit reasons.
2. Перестроить tree в AST с поддержкой multiple argument branches + predicates.
3. Сделать pluggable parser/completion engine на основе одного source of truth (общий command grammar graph).
4. Вынести framework в отдельный versioned artifact + compatibility policy.

---

## 11) Конкретные рефактор-стратегии (G)

### 11.1 Ввести `CommandContext` v2

- Typed slots вместо `Map<String,Object>`:
  - `SenderContext`,
  - `ArgumentBag` (typed accessors),
  - `RequestMetadata`.
- Запретить произвольный `services` map в request context.

### 11.2 Ввести `ExecutionPipeline` с middleware layer

- Middleware получает `ExecutionEnvelope` и может:
  - `continue`,
  - `fail(CommandError)`,
  - `defer(CompletionStage<...>)`.
- Stage должны быть side-effect isolated, а platform effects только в terminal dispatcher.

### 11.3 Отделить core от Bukkit adapter

- `command-core-engine`: tree, parser, validation, execution contracts.
- `command-paper-adapter`: sender adaptation, scheduler bridge, command binding, suggestions bridge.
- Никаких Paper-классов в core API и никакого downcast-а обратно.

### 11.4 Улучшить dependency inversion

- Внедрять зависимости через constructor-injected modules/factories.
- Убрать `new` конкретных зависимостей внутри binder/bootstrap.
- Ввести конфигурируемые policies: executor, metrics, localization, error policy.

---

## 12) Оценка зрелости архитектуры

**4.5 / 10.**

Причины:
- плюс: модульная раскладка, базовый pipeline/middleware каркас, async-сигнатуры;
- минус: протекающие абстракции, недоделанная completion-интеграция, слабая observability, service-locator и lifecycle-долги, ограничения command tree, слабая scalability story.

Текущая система годится как внутренний прототип, но не как production-grade фреймворк уровня ACF/Cloud.
