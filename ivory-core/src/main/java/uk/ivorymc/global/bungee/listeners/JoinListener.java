package uk.ivorymc.global.bungee.listeners;

import community.leaf.textchain.adventure.TextChain;
import me.justeli.sqlwrapper.SQL;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import uk.ivorymc.api.storage.SQLController;
import uk.ivorymc.global.bungee.IvoryBungee;

import java.util.ArrayList;
import java.util.List;

public record JoinListener(IvoryBungee plugin) implements Listener
{
    @EventHandler
    public void onJoin(PostLoginEvent event)
    {
        ProxiedPlayer player = event.getPlayer();
        SQLController sqlController = plugin.getSqlController();
        // create player row in db
        sqlController.sql().query(
            "INSERT IGNORE INTO player(uuid, name) values(?,?)",
            SQL.uuidToBytes(player.getUniqueId()),
            player.getName()
        ).queue();
        // check for mail
        List<TextChain> mail = new ArrayList<>();
        // get all mail from the mail table that's for the player or global
        // along with the sender's name from the player table via an inner join
        sqlController.sql().query(
            "SELECT mail.sender_uuid, mail.message as message, mail.date as date, player.name as sender_name " +
            "FROM mail WHERE mail.recipient_uuid = ? AND mail.sender_uuid != ?" +
            "INNER JOIN player ON mail.sender_uuid=player.uuid",
            SQL.uuidToBytes(player.getUniqueId()),
            SQL.uuidToBytes(player.getUniqueId())
        ).select().queue(result -> {
            while (result.next())
            {
                mail.add(
                    TextChain.chain()
                        .then(result.getString("message"))
                        .nextLine()
                        .then("From: ")
                        .then(result.getString("sender_name"))
                        .then(", ")
                        .then(result.getTimestamp("date").toString())
                );
            }
        });
        // send any found mail
        mail.forEach(message -> message.send(plugin.adventure().sender(player)));
    }
}
