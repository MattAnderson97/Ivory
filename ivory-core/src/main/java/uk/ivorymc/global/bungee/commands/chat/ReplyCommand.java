package uk.ivorymc.global.bungee.commands.chat;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import uk.ivorymc.global.bungee.IvoryBungee;
import uk.ivorymc.global.bungee.commands.templates.PlayerCommand;

public class ReplyCommand extends PlayerCommand
{
    public ReplyCommand(IvoryBungee plugin)
    {
        super("reply", plugin);
    }

    @Override
    public void run(ProxiedPlayer player, String[] args)
    {

    }
}