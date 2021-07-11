package uk.ivorymc.global.bungee.listeners;

import me.justeli.sqlwrapper.SQL;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import uk.ivorymc.api.storage.SQLController;
import uk.ivorymc.global.bungee.IvoryBungee;

import java.util.concurrent.atomic.AtomicBoolean;

public record JoinListener(IvoryBungee plugin) implements Listener
{
    @EventHandler
    public void onJoin(PostLoginEvent event)
    {
        ProxiedPlayer player = event.getPlayer();
        /*
         * BungeePlayerData playerData = plugin.getPlayerData(player);
         * if (!playerData.isSet("join-date"))
         * {
         *     playerData.write("join-date", Utils.dateString());
         * }
         */
        SQLController sqlController = plugin.getSqlController();

        sqlController.sql().query(
                "INSERT IGNORE INTO player(uuid, name) values(?,?)",
                SQL.uuidToBytes(player.getUniqueId()),
                player.getName()
        ).complete();
    }
}
