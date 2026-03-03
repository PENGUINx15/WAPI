package me.penguinx13.wapi.showcase.core.resolver;

import me.penguinx13.wapi.commands.core.context.CommandContext;
import me.penguinx13.wapi.commands.core.error.UserInputException;
import me.penguinx13.wapi.commands.core.metadata.ArgumentMetadata;
import me.penguinx13.wapi.commands.core.resolver.ArgumentResolver;
import me.penguinx13.wapi.showcase.core.db.EnterpriseUserRepository;
import me.penguinx13.wapi.showcase.core.model.EnterpriseUser;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public final class EnterpriseUserResolver implements ArgumentResolver<EnterpriseUser> {
    private final EnterpriseUserRepository repository;

    public EnterpriseUserResolver(EnterpriseUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public Class<EnterpriseUser> supports() {
        return EnterpriseUser.class;
    }

    @Override
    public int priority() {
        return 250;
    }

    @Override
    public boolean canResolve(ArgumentMetadata argumentMetadata) {
        return EnterpriseUser.class == argumentMetadata.type();
    }

    @Override
    public CompletionStage<EnterpriseUser> parse(String input, ArgumentMetadata metadata, CommandContext context) {
        return repository.findByName(input).thenCompose(user ->
                user.<CompletionStage<EnterpriseUser>>map(CompletableFuture::completedFuture)
                        .orElseGet(() -> CompletableFuture.failedStage(new UserInputException("User not found: " + input))));
    }

    @Override
    public CompletionStage<List<String>> suggest(String input, ArgumentMetadata metadata, CommandContext context) {
        return repository.suggestNames(input == null ? "" : input, 20);
    }
}
