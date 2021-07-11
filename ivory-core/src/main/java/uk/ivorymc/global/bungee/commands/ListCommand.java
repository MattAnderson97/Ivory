package uk.ivorymc.global.bungee.commands;

import community.leaf.textchain.adventure.TextChain;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import uk.ivorymc.global.bungee.IvoryBungee;

import java.util.ArrayList;
import java.util.List;

public class ListCommand extends Command
{
    private final IvoryBungee plugin;

    public ListCommand(IvoryBungee plugin)
    {
        super("list", "", "players");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        List<String> players = new ArrayList<>();
        plugin.getProxy().getPlayers().forEach(player -> players.add(player.getName()));

        TextChain.chain()
            .then(String.join(", ", players))
            .send(plugin.adventure().sender(sender));
    }
}
