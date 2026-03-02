# WAPI Command Framework V2

## Textual architecture diagram

```
plugin (integration)
  -> command-paper-adapter
      -> command-core
          -> command-annotations
```

Execution flow:
`Paper command -> CommandContext -> Pipeline (Routing -> Parsing -> Validation -> Authorization -> Cooldown -> Invocation -> PostProcessing -> ErrorHandling)`.

## Module structure

- `command-annotations`: annotation model (`@RootCommand`, `@SubCommand`, `@Arg`, `@Range`, `@Min`, `@Max`, `@Regex`).
- `command-core`: metadata cache, immutable command tree, resolver registry, validation service, pipeline, typed errors, completion engine, platform interfaces.
- `command-paper-adapter`: Paper sender adapters, scheduler, logger, audience, permission, error presenter.
- `plugin`: real plugin wiring and command registration.

## Migration guide (old -> new)

1. Move command annotations imports to `me.penguinx13.wapi.commands.annotations.*`.
2. Replace `CommandRegistry` usage with `CommandFrameworkBootstrap`.
3. Use `CooldownManager#isOnCooldown` + `markCooldown` semantics.
4. Replace `SQLiteManager#executeQuery` raw `ResultSet` usage with `query(sql, params, mapper)`.
5. Register custom resolvers through `ResolverRegistry#register` with priorities.
6. Register custom validation rules through `ValidationService#register`.

## Performance evaluation notes (target scale)

- 100+ commands: immutable `CommandTree` provides O(depth) routing and lock-free reads.
- 50+ concurrent players: runtime registries use concurrent collections and immutable snapshots for reads.
- Heavy tab completion: `CompletionEngine` keeps per-request suggestion cache and async resolver API.

## Trade-offs

- Chosen reflection-based metadata scanning for annotation compatibility; startup overhead increases slightly, runtime cost drops due to immutable caching.
- Pipeline stage classes are explicit and extensible, but setup is more verbose.
- `CompletableFuture` suggestion/query APIs increase complexity but prevent main-thread blocking at scale.
