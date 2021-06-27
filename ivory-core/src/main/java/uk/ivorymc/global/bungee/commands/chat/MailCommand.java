package uk.ivorymc.global.bungee.commands.chat;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import uk.ivorymc.global.bungee.IvoryBungee;

public class MailCommand extends Command
{
    private final IvoryBungee plugin;

    public MailCommand(IvoryBungee plugin)
    {
        super("mail");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender commandSender, String[] args)
    {

    }
}
