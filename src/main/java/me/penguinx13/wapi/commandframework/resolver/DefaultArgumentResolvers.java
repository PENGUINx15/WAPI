package me.penguinx13.wapi.commandframework.resolver;

public final class DefaultArgumentResolvers {
    private DefaultArgumentResolvers() {
    }

    public static void registerDefaults(ResolverRegistry registry) {
        registry.register(new StringArgumentResolver());
        registry.register(new IntegerArgumentResolver());
        registry.register(new DoubleArgumentResolver());
        registry.register(new BooleanArgumentResolver());
        registry.register(new PlayerArgumentResolver());
        registry.register(new UUIDArgumentResolver());
        registry.register(new EnumArgumentResolver());
    }
}
