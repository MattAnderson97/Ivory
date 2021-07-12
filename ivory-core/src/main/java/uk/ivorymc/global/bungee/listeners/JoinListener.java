package uk.ivorymc.global.bungee.listeners;

import community.leaf.textchain.adventure.TextChain;
import me.justeli.sqlwrapper.SQL;
import net.kyori.adventure.text.format.TextColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import uk.ivorymc.api.storage.SQLController;
import uk.ivorymc.global.bungee.IvoryBungee;

import java.util.concurrent.TimeUnit;

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
    }

    @EventHandler
    public void onServerJoin(ServerConnectedEvent event)
    {
        ProxiedPlayer player = event.getPlayer();

        // run delayed task (sync)
        plugin.getProxy().getScheduler().schedule(plugin,
            // set anonymous runnable method to the query
            () -> plugin.getSqlController().sql().query(
                "SELECT EXISTS(SELECT * FROM mail WHERE recipient_uuid like ?) AS 'exists';", // is there any mail for the player
                new Object[]{SQL.uuidToBytes(player.getUniqueId())}
            ).select().queue(result -> {
                // check if the result is true or false
                result.next();
                if (result.getBoolean("exists"))
                {
                    // send prompt to player
                    TextChain.chain()
                        .then("You have unread messages. Read them with /mail read")
                            .color(TextColor.color(0x03DAC6))
                        .send(plugin.adventure().sender(player));
                }
            }), 1000, TimeUnit.MILLISECONDS
        );
        //plugin.getMailHandler().readMail(player);
    }
}
