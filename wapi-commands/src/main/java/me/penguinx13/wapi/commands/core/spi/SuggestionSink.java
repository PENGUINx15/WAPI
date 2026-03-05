package me.penguinx13.wapi.commands.core.spi;

import java.util.List;

@FunctionalInterface
public interface SuggestionSink {
    void accept(List<String> suggestions);
}
