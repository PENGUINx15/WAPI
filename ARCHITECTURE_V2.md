# WAPI Command Architecture V2

## 1) Runtime Architecture (text diagram)

```
Paper/Bukkit CommandMap
   -> PaperCommandBinder
      -> CommandRuntime.executeAndRespond()
         -> MiddlewareChain
            -> CommandPipeline
               [Routing -> Parsing -> Validation -> Condition/Auth -> Cooldown -> Invocation -> Post]
         -> ErrorPresenter
         -> PlatformBridge.deliverResult()
```

### Runtime ownership
`CommandRuntime` owns:
- `CommandTree`
- `CommandPipeline`
- `ResolverRegistry`
- `ValidationService`
- `ErrorPresenter`
- `MiddlewareChain` input list
- `PlatformBridge`
- `MetricsSink`

## 2) Module structure

- `command-core`: platform-agnostic runtime, tree, pipeline, middleware, resolver SPI, validation SPI, metrics SPI.
- `command-annotations`: annotation command model.
- `command-paper-adapter`: Paper sender adapters, binders, schedulers, player resolver, error presenter.
- `plugin` (`WAPI`): bootstrap and plugin wiring.

## 3) Key implementations

- Async-first `CommandStage` + sealed `StageResult`.
- `CommandContext` immutable; `ExecutionState` mutable request-scope state.
- Deterministic tree routing with literal-first then argument edge.
- Resolver V2: assignable types, primitive-wrapper normalization, enum fallback.
- Middleware lifecycle via `CommandMiddleware#handle(CommandInvocation, MiddlewareChain)`.
- Error hierarchy rooted at `CommandException`; `Error` is rethrown.

## 4) Example command definition

```java
@RootCommand("main")
class MainCommand {
  @SubCommand("give")
  public void give(Player sender, @Arg("target") Player target, @Arg("amount") int amount) {}
}
```

## 5) Example Paper binding

`CommandFrameworkBootstrap#buildAndBind()`:
1. Build tree from scanned metadata.
2. Build runtime.
3. Create `PaperCommandBinder`.
4. Register Bukkit executor for root command from `plugin.yml`.

## 6) Migration guide

1. Replace direct `CommandPipeline` usage with `CommandRuntime`.
2. Move mutable request data from context to `ExecutionState`.
3. Register resolvers through `ResolverRegistry.register(...)`.
4. Replace legacy exceptions with `CommandException` subtypes.
5. Integrate platform response through `PlatformBridge`.

## 7) Thread-safety model

- `CommandContext`: immutable, thread-safe.
- `ExecutionState`: request-local mutable maps only.
- Async command execution via explicit scheduler/executor.
- SQLite uses dedicated single-thread executor and no shared global connection.
- Main-thread DB access is rejected.

## 8) Performance characteristics

- O(depth) deterministic routing.
- Literal map lookup on hot path, no stream scanning.
- Async pipeline avoids blocking main thread for async-capable handlers.
- Suggestion cache in execution state keyed by full path + raw input.

## 9) Testing strategy

- Unit: tree conflict detection, resolver matching order, validator behavior.
- Unit: pipeline short-circuit + cancellation.
- Integration: command invoke from Paper sender adapter.
- Concurrency: completion/cache under parallel invocations.
- Failure-path: infrastructure exceptions and presenter behavior.

## 10) Comparison vs original WAPI

| Area | Old | V2 |
|---|---|---|
| Pipeline | sync, mutable | async-first, structured `StageResult` |
| Runtime | implicit wiring | explicit `CommandRuntime` orchestration |
| Tree | stream scans | deterministic maps/lists |
| Resolver | exact class only | assignable + enum fallback |
| Middleware | weak before/after hooks | lifecycle chain with override/cancel |
| Errors | catch-all `Throwable` | strict hierarchy + fatal error propagation |
| SQLite | common-pool + shared connection | single-thread DB executor, per-op connection |
| Platform boundary | core leaks risk | hard SPI (`PlatformBridge`, binder in adapter) |
