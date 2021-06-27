package uk.ivorymc.global.bungee.listeners;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import uk.ivorymc.global.bungee.IvoryBungee;

public record QuitListener(IvoryBungee plugin) implements Listener
{
    @EventHandler
    public void onQuit(PlayerDisconnectEvent event)
    {
        ProxiedPlayer player = event.getPlayer();
        plugin.removeReply(player);
        plugin.getPlayerData(player).saveAsync();
    }
}
