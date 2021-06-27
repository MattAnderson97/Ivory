package uk.ivorymc.global.bungee.commands.templates;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import uk.ivorymc.global.bungee.IvoryBungee;

public abstract class PlayerCommand extends Command
{
    protected final IvoryBungee plugin;

    public PlayerCommand(String name, IvoryBungee plugin)
    {
        super(name);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender commandSender, String[] args)
    {
        if (!(commandSender instanceof ProxiedPlayer))
        {
            return;
        }
        run((ProxiedPlayer) commandSender, args);
    }

    protected abstract void run(ProxiedPlayer player, String[] args);
}
