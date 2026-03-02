# Жёсткий архитектурный аудит WAPI (Paper plugin + custom command framework)

## 1) Слойность и границы

### 1.1 «Core» не является core
`commandframework` напрямую зависит от Bukkit API (`CommandSender`, `Player`, `ChatColor`, `Bukkit`) в диспетчере, метадате и резолверах. Это не фреймворк, а плагин-утилита, вшитая в Paper. Вынести это в отдельный модуль без боли нельзя.

- `CommandDispatcher` типами и логикой привязан к Bukkit (`CommandSender`, `Player`, `ChatColor`) и даже формирует текст помощи сам.  
- `ArgumentResolver` API требует `CommandSender` прямо в интерфейсе.  
- `CommandMetadataCache` знает про `Player`/`CommandSender` и кодирует это в правилах сканирования.

Итог: слой «domain/framework core» отсутствует. Есть один слипшийся слой «Bukkit + reflection + parsing + UX». 

### 1.2 Риск God-class
`CommandDispatcher` — жирный комбайн из:
- маршрутизации,
- авторизации,
- help-рендеринга,
- валидации аргументов,
- преобразования аргументов,
- reflection invoke,
- таб-комплита.

Это прямое нарушение SRP: изменение любой из подсистем требует править один и тот же класс.

### 1.3 Неопределённые границы ответственности
`CommandRegistry` инициализирует дефолтные резолверы, строит дерево, ловит ошибки, связывается с `plugin.yml`, биндингует executor/tab completer, хранит mutable error handler. Это composition root + runtime registry + adapter binder + error boundary одновременно.

## 2) Командный фреймворк

### 2.1 Dispatcher design: процедурный монолит
Пайплайна нет. Нет стадий `Parse -> Validate -> Authorize -> Execute -> HandleError`. Из-за этого:
- нельзя добавить middleware (логирование, rate-limit, audit trail, metrics) без прямого вторжения в `CommandDispatcher`;
- нельзя изолированно тестировать стадии;
- нельзя переиспользовать parse/validate отдельно от invoke.

### 2.2 CommandTree: простой trie без конфликт-стратегии
`CommandTree.add` тупо ставит `current.setCommand(meta)` на финальном узле. Если две команды имеют одинаковый путь — последняя молча перезапишет первую. Никакой диагностики конфликта, никакой политики merge/reject.

### 2.3 Metadata cache: формально cache, фактически trap
Кэш ключуется `Class<?>`, а `CommandMethodMeta` хранит `instance`. При повторной регистрации второго инстанса того же класса вы получите метадату со старым instance из первого скана (утечка состояния и неправильный target invoke).

Это критическая архитектурная ошибка: кэш immutable-метадаты смешан с объектом runtime-инстанса.

### 2.4 Argument resolution: O(N) linear scan и неявный precedence
`ResolverRegistry.find` линейно проходит список `CopyOnWriteArrayList`. При росте числа резолверов таб-комплит/диспетч будет постоянно платить O(N). Порядок регистрации решает всё; конфликты `supports()` не детектятся.

### 2.5 Tab completion архитектура: неполная модель
- Нет понятия контекста выполнения (sender, path, parsed args, flags) как отдельного объекта.
- Нет async completion API.
- Нет батчинга/кэширования expensive suggestions (напр. lookup игроков/БД).
- Возврат `Collections.emptyList()` на любом сбое глушит диагностику.

### 2.6 Error propagation: «тихие» провалы
`tabComplete()` в `CommandRegistry` ловит `Exception` и молча возвращает пустой список. Это скрывает реальные ошибки и превращает дебаг в гадание.

### 2.7 Validation model: примитивный и локальный
Сейчас есть только `@Range` для `Number` и ручная проверка внутри `CommandDispatcher`. Нельзя добавлять валидаторы декларативно, нет цепочки валидаторов, нет групп/контекстов, нет международных сообщений об ошибках.

## 3) Архитектурные smell’ы

### 3.1 Mixed concerns
- `MessageManager` одновременно: шаблонизатор, PlaceholderAPI интеграция, MiniMessage/legacy parsing, action/title/subtitle/json роутинг, логгер.
- `SQLiteManager` одновременно: lifecycle connection, SQL executor, statement factory, sync DAO gateway.

### 3.2 Hidden state / mutable global-ish state
- `CommandRegistry#errorHandler` mutable и `volatile`; поведение фреймворка может меняться в рантайме из другой части плагина.
- `CooldownManager` держит mutable `HashMap` без синхронизации и без TTL cleanup — потенциально бесконечный рост.

### 3.3 Overuse/unsafe reflection
- `CommandMetadataCache` жёстко relies on reflection (`setAccessible(true)`), что ломается на stricter runtime policies.
- `CustomSkulls` рефлексией лезет в private поле `profile`, что хрупко к версии сервера.

### 3.4 Leaky abstractions
- Абстракция резолвера протекает Bukkit-типом sender через весь API.
- `CommandException` одновременно и control-flow (ожидаемые бизнес-ошибки), и internal error wrapper.

### 3.5 Poor boundary definitions
Нет модуля `api`/`core`/`paper-adapter`. Всё в одном `src/main/java`, ответственность не сегментирована пакетами уровня архитектуры.

## 4) Производительность и масштаб

### 4.1 Reflection runtime invoke на каждом выполнении
Каждый command call использует `Method.invoke`. Для 50+ игроков и частых команд это избыточный overhead. Нет перехода на prebound `MethodHandle`/лямбда-bridge.

### 4.2 Resolver lookup и suggestions
- Каждый аргумент — линейный поиск резолвера.
- `PlayerArgumentResolver.suggest` каждый раз итерирует всех онлайновых игроков.
- Нет кэширования suggestions даже в пределах одного tab-complete цикла.

### 4.3 Help rendering рекурсивно без ограничения
На дереве из 100+ команд help-генерация может создавать большой объём строк каждый вызов `/root`/`help`, без paging.

### 4.4 Blocking IO риск на main thread
`SQLiteManager` полностью синхронный. Если его использовать из команд/ивентов (а так и будет), I/O пойдёт в тике сервера. Это лаги/таймауты под нагрузкой.

### 4.5 Resource leak в query API
`SQLiteManager.executeQuery` возвращает `ResultSet`, созданный через `createStatement()`, но `Statement` не закрывается вызываемой стороной автоматически. Это дыра в управлении ресурсами.

### 4.6 Неправильная семантика cooldown
`CooldownManager.hasCooldown` возвращает `true`, когда cooldown записи нет. Это контринтуитивно и архитектурно токсично: API врёт по имени, что генерирует баги в use-site.

## 5) SOLID: нарушения

### S — Single Responsibility
- `CommandDispatcher` и `MessageManager` нарушают SRP в лоб.

### O — Open/Closed
- Добавление нового этапа выполнения команды требует правки `CommandDispatcher`.
- Добавление новой политики ошибок/валидации/permission flow не расширением, а модификацией.

### L — Liskov
Явных проблем меньше, но контракт `ArgumentResolver` по факту не стабилен: часть резолверов делает «дешёвый parse», часть делает внешний lookup (player), что ломает ожидания по latency/side-effects.

### I — Interface Segregation
`ArgumentResolver` склеивает parse + suggest + sender-aware контекст в одном интерфейсе. Для простых scalar resolver’ов sender вообще не нужен.

### D — Dependency Inversion
`CommandRegistry` и core-классы завязаны на конкретный `JavaPlugin`/Bukkit типы, вместо абстракций платформы. DIP фактически отсутствует.

## 6) Thread-safety

- `CommandTree` использует `ConcurrentHashMap`, но дерево как структура не имеет transactional consistency при параллельной регистрации/чтении.
- `CooldownManager` не потокобезопасен (`HashMap`), что станет гонкой при любом async использовании.
- `SQLiteManager` хранит один shared `Connection` без стратегии конкурентного доступа; JDBC connection обычно не thread-safe для такого режима использования.

## 7) Extensibility

### 7.1 Новые аргументы
Технически добавить можно, но архитектурно неудобно:
- нет type-keyed registry,
- нет приоритетов/override policy,
- нет scoped resolver’ов (per-command/per-context).

### 7.2 Middleware
Невозможно встраивать cleanly (нет pipeline/interceptor chain).

### 7.3 Async execution
Нет execution model. Любой async today = руками внутри command method с нарушением thread-правил Bukkit.

### 7.4 Extract как standalone
С текущими зависимостями на Bukkit в ядре — практически нет. Нужно разрезать проект по модулям.

## 8) Что развалится в production (100+ команд, 50+ игроков)

1. Дебаг сломается из-за «тихого» проглатывания ошибок таб-комплита. 
2. Латентность вырастет из-за линейных lookup резолверов и рекурсивного help на больших деревьях. 
3. Командная база начнёт ловить коллизии путей без детекции (тихое перезаписывание). 
4. SQLite на main thread даст микрофризы/лагающие тики. 
5. Появятся race conditions при попытке вынести операции в async (cooldown/sqlite shared state).

## 9) Сравнение с ACF / Cloud Command Framework

Сейчас конкуренции нет.

Где проигрывает:
- отсутствует строгая модель контекста команды,
- отсутствует middleware/pipeline,
- нет brigadier-level модели suggestions,
- нет зрелой exception taxonomy + message bundles + i18n,
- нет DI-интеграции,
- нет модульности core/platform,
- нет асинхронного execution contract,
- нет declarative constraints/condition system,
- нет robust test surface.

Почему это критично: ACF/Cloud — это инфраструктура с расширяемыми точками. Здесь — набор utility-классов.

## 10) Приоритетный roadmap

### Immediate (high impact / low risk)
1. Починить `CommandMetadataCache`: отделить immutable metadata (на class/method) от binding instance.  
2. Убрать `catch (Exception)` в tabComplete, логировать и возвращать безопасно диагностируемый результат.  
3. В `CommandTree.add` добавить детекцию конфликтов пути с явным исключением.  
4. Исправить API `CooldownManager` (`isOnCooldown` semantics), заменить `HashMap` на `ConcurrentHashMap`, добавить cleanup.
5. В `SQLiteManager` запретить sync usage на main thread (guard + warning), исправить resource lifecycle (`ResultSet` wrapper/consumer API).

### Medium-term refactor
1. Ввести `CommandContext` (sender, root, path, rawArgs, parsedArgs, metadata, services).  
2. Разбить `CommandDispatcher` на сервисы: `CommandRouter`, `AccessEvaluator`, `ArgumentParser`, `Invoker`, `HelpRenderer`, `CompletionEngine`.  
3. Переделать `ResolverRegistry` в map-based индекс (`Class<?> -> ResolverChain`) + приоритеты.  
4. Ввести `ValidationService` (range + custom validators).

### Major redesign
1. Модульная архитектура:
   - `command-core` (без Bukkit),
   - `command-paper-adapter`,
   - `command-annotations`.
2. ExecutionPipeline/Interceptor chain:
   - pre-parse,
   - pre-execute,
   - post-execute,
   - on-error.
3. Async command contract (`CompletionStage<Result>` + scheduler bridge к Bukkit main thread).
4. Condition/Requirement system (permissions, sender type, world/region constraints) как расширяемые политики.

## 11) Конкретные стратегии рефакторинга

### 11.1 CommandContext
Вместо рассыпанных параметров и локальных переменных ввести immutable context + mutable execution bag.

### 11.2 ExecutionPipeline
Интерфейс `CommandInterceptor`:
- `beforeParse(ctx)`
- `beforeExecute(ctx)`
- `afterExecute(ctx, result)`
- `onError(ctx, throwable)`

Это откроет логирование, cooldown middleware, метрики, транзакционные обёртки.

### 11.3 Core vs Bukkit adapter
- В core: `Sender`, `PermissionChecker`, `Audience`, `CommandPlatformScheduler` — абстракции.
- В adapter: маппинг на Bukkit `CommandSender/Player`.

### 11.4 Dependency inversion
`CommandRegistry` не должен принимать `JavaPlugin`. Он должен принимать интерфейсы `PlatformCommandBinder`, `Logger`, `Scheduler`, `AudienceProvider`.

### 11.5 Typed error model
Вместо одного `CommandException` ввести категории:
- `UserInputError`
- `AuthorizationError`
- `ExecutionError`
- `InfrastructureError`

И единый `ErrorPresenter`, независимый от Bukkit ChatColor.

## 12) Оценка зрелости архитектуры

**3.2 / 10**

Обоснование:
- Есть зачатки структуры (аннотации, дерево, резолверы, error handler).
- Но нет модульности, нет зрелой модели расширения, есть критический баг в metadata cache, нет безопасной стратегии работы с БД, и core не отделён от платформы.
- Для production-grade командного фреймворка это уровень прототипа, а не инфраструктурного компонента.
