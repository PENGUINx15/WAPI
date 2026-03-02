package me.penguinx13.wapi.commands.core.spi;

import me.penguinx13.wapi.commands.core.runtime.CommandRuntime;

public interface PlatformCommandBinder {
    void bind(CommandRuntime runtime);
}
