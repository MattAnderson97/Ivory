package uk.ivorymc.global.bungee.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import uk.ivorymc.global.bungee.IvoryBungee;

public class LogCommand extends Command
{
    private final IvoryBungee plugin;

    public LogCommand(IvoryBungee plugin)
    {
        super("log");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings)
    {
        // do log command stuffs
    }
}
