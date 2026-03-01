package me.penguinx13.wapi.commandframework;

/**
 * Mini-framework usage notes:
 * <pre>
 * Registration in onEnable():
 * CommandRegistry registry = new CommandRegistry(this);
 * registry.registerCommands(new MainCommand(this));
 *
 * Nested tab completion examples:
 * /main a<TAB>              -> admin
 * /main admin r<TAB>        -> reset
 * /main give <TAB>          -> online player names
 * /main debug mode <TAB>    -> off, basic, verbose
 * /main give Steve 10 <TAB> -> true, false, &lt;skip&gt;
 *
 * Auto-help examples:
 * /main                     -> full tree help
 * /main help                -> full tree help
 * /main admin help          -> help under "admin"
 *
 * Help shows hierarchical entries, generated usage and required permissions.
 * </pre>
 */
public final class CommandFrameworkExamples {
    private CommandFrameworkExamples() {
    }
}
