package uk.ivorymc.global.bungee.listeners;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import uk.ivorymc.api.playerdata.BungeePlayerData;
import uk.ivorymc.api.utils.Utils;
import uk.ivorymc.global.bungee.IvoryBungee;

public record JoinListener(IvoryBungee plugin) implements Listener
{
    @EventHandler
    public void onJoin(PostLoginEvent event)
    {
        ProxiedPlayer player = event.getPlayer();
        BungeePlayerData playerData = plugin.getPlayerData(player);
        if (!playerData.isSet("join-date"))
        {
            playerData.write("join-date", Utils.dateString());
        }
    }
}
