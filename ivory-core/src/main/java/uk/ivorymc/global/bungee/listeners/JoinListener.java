package uk.ivorymc.global.bungee.listeners;

import me.justeli.sqlwrapper.SQL;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import uk.ivorymc.api.storage.SQLController;
import uk.ivorymc.global.bungee.IvoryBungee;

import java.sql.Date;

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
            "SELECT EXISTS(SELECT * FROM player WHERE name LIKE ?) AS 'exists';",
            player.getName()
        ).select().queue(
            resultSet -> {
                if (!resultSet.first())
                {
                    resultSet.close();
                    sqlController.sql().query(
                        "INSERT INTO player(uuid, name, join_date) values(?,?,?)",
                        SQL.uuidToBytes(player.getUniqueId()),
                        player.getName(),
                        new Date(new java.util.Date().getTime()) // new sql date based on unix timestamp
                    ).queue();
                }
            }
        );
    }
}
