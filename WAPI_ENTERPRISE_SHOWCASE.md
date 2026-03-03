# WAPIEnterpriseShowcase

## 1) Module structure
- `command-annotations`: command + validation annotations (`@RootCommand`, `@SubCommand`, `@Arg`, `@Min`, `@Max`, `@Range`, `@Regex`, `@NotReservedName`).
- `command-core`: scanning, routing, parsing, validation, middleware chain, error hierarchy, metrics sink, execution state.
- `command-paper-adapter`: paper sender resolver, player resolver, scheduler, bridge, binder.
- `wapi-example`: enterprise showcase + velocity example plugin, command implementations, SQLite repository, middleware, platform lifecycle.

## 2) Main classes
- Paper main: `me.penguinx13.wapi.WAPI`
- Paper bootstrap: `me.penguinx13.wapi.showcase.paper.EnterprisePaperBootstrap`
- Velocity main: `me.penguinx13.wapi.showcase.velocity.WAPIEnterpriseShowcaseVelocity`

## 3) Command classes
- Root `/enterprise`: `me.penguinx13.wapi.showcase.commands.EnterpriseCommand`
- Nested subcommands: `user create/info/delete`, `debug uuid/mode`, `tree scan`, `skull give`, `entity name`, `stats`, `heavy`, `reload`, `give`.

## 4) Middleware implementations
- `LoggingMiddleware`: correlation-id + latency logs.
- `AuditMiddleware`: sender + command + success logs.
- `ErrorMappingMiddleware`: wraps non-fatal exceptions into `InfrastructureException`.
- `DeleteUserCooldownMiddleware`: 5s cooldown for `/enterprise user delete`.

## 5) Custom resolver example
- `EnterpriseUserResolver` resolves `EnterpriseUser` from SQLite and provides async name suggestions.

## 6) Custom validator example
- `@NotReservedName` + `NotReservedNameValidator` blocks reserved names.

## 7) DB schema initialization
- `EnterpriseUserRepository#initializeSchema`: `CREATE TABLE IF NOT EXISTS users (name TEXT PRIMARY KEY, age INTEGER NOT NULL)`.
- Dedicated executor, connection-per-operation, async CompletionStage API.

## 8) Config examples
- `wapi-example/src/main/resources/messages.yml`
- `wapi-example/src/main/resources/database.yml`

## 9) Platform binding code
- Paper binding: `EnterprisePaperBootstrap` uses `PaperCommandBinder` + `PaperPlatformBridge`.
- Velocity lifecycle class provided as abstraction showcase entry point.

## 10) Feature coverage map
- Annotation command API + nested subcommands: `EnterpriseCommand`
- Argument parsing + resolver system: String/int/boolean/UUID/enum fallback/Player/custom user resolver
- Validation: `@Min`, `@Max`, `@Range`, `@Regex`, `@NotReservedName`
- Completion: resolver suggestions + async DB suggestions, cached in `ExecutionState`
- Middleware: logging, audit, error mapping, cooldown middleware
- Metrics: `EnterpriseMetricsSink`, exposed by `/enterprise stats`
- Error handling hierarchy: user, validation, authorization, cooldown, infrastructure errors
- SQLite async persistence: `EnterpriseUserRepository`
- MessageManager + ConfigManager: reload + formatted/action-bar placeholders
- Utility APIs: `Tree`, `CustomSkulls`, `EntityName`
- Custom events: `FallOnVoidEvent` handled by `EnterpriseVoidGuardListener`
- Platform abstraction: Paper runtime + Velocity bootstrap class

## 11) Thread-safety
- All DB work happens on dedicated executor (`enterprise-sqlite`) and each operation opens its own connection.
- Heavy workload command runs on dedicated executor (`enterprise-heavy`) and returns to main thread only for player messaging.
- Cooldowns/metrics use concurrent structures or atomics.
- No global mutable static state for runtime services.

## 12) Load behavior (100+ commands, 50+ players)
- Middleware overhead is O(1) per command; metrics/correlation logging stay constant-time.
- Resolver registry lookup is bounded and cached by metadata usage patterns.
- Async user suggestions avoid main-thread DB stalls for tab completion bursts.
- Connection-per-operation avoids shared mutable JDBC connection contention.
- Heavy operations and SQL are isolated from tick thread, preserving TPS under concurrent command usage.
