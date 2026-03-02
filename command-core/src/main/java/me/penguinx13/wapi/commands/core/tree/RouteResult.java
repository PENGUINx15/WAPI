package me.penguinx13.wapi.commands.core.tree;

import me.penguinx13.wapi.commands.core.metadata.BoundCommandMethod;

import java.util.List;
import java.util.Map;

public record RouteResult(BoundCommandMethod command, List<String> consumedPath, Map<String, String> capturedArguments) {}
