package uk.ivorymc.global.bungee.listeners;

import me.justeli.sqlwrapper.SQL;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import uk.ivorymc.global.bungee.IvoryBungee;
import uk.ivorymc.global.bungee.events.PlayerCommandEvent;

public record CommandListener(IvoryBungee plugin) implements Listener
{
    @EventHandler
    public void onCommand(PlayerCommandEvent event)
    {
        ProxiedPlayer player = event.getPlayer();
        String command = event.getCommand();
        plugin.getSqlController().sql().query(
            /* "INSERT INTO command_logs(message,player_uuid) VALUES(?,(SELECT uuid FROM player WHERE uuid = ?));" */
            "INSERT INTO command_logs(message, player_uuid) VALUES(?,?);",
            command,
            SQL.uuidToBytes(player.getUniqueId())
        ).queue();
    }
}
