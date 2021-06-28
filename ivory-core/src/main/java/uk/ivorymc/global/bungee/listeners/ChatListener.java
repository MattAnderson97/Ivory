package uk.ivorymc.global.bungee.listeners;

import community.leaf.textchain.adventure.TextChain;

import me.justeli.sqlwrapper.SQL;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import uk.ivorymc.api.playerdata.BungeePlayerData;
import uk.ivorymc.global.bungee.IvoryBungee;
import uk.ivorymc.global.bungee.events.PlayerCommandEvent;

public record ChatListener(IvoryBungee plugin) implements Listener
{
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(ChatEvent event)
    {
        if (!(event.getSender() instanceof ProxiedPlayer player)) { return; }
        if (event.isCommand() || event.isProxyCommand())
        {
            plugin.getProxy().getPluginManager().callEvent(new PlayerCommandEvent(player, event.getMessage()));
            return;
        }

        event.setCancelled(true);

        BungeePlayerData playerData = plugin.getPlayerData(player);

        String originalMessage = event.getMessage();

        Server currentServer = player.getServer();
        String serverName = currentServer.getInfo().getName();

        TextChain chatMessage = TextChain.chain()
            .then(player.getDisplayName())
                .color(NamedTextColor.GRAY)
            .tooltip(tooltip -> tooltip
                .then(player.getName())
                .nextLine()
                .then(" ")
                .then("     ")
                    .color(TextColor.color(0x03DAC6))
                    .strikethrough()
                .nextLine()
                .then("Server: ")
                    .color(TextColor.color(0x03DAC6))
                .then(serverName)
                    .color(NamedTextColor.WHITE)
                .nextLine()
                .then("Join date: ")
                    .color(TextColor.color(0x03DAC6))
                .then(playerData.getConfig().getString("join-date"))
                    .color(NamedTextColor.WHITE)
            )
            .then(" >")
                .color(TextColor.color(0x018786))
            .then("> ")
                .color(TextColor.color(0x03DAC6))
            .then(originalMessage)
                .color(NamedTextColor.WHITE)
                .tooltip("Click to send a message")
                .suggest("/msg " + player.getName() + " ");

        plugin.getProxy().getPlayers().forEach(
            loopPlayer -> chatMessage.send(plugin.adventure().player(loopPlayer))
        );

        // write to chat logs
        plugin.getSqlController().sql().query(
            "INSERT INTO chat_logs(player_uuid, message) VALUES(?, ?)",
            SQL.uuidToBytes(player.getUniqueId()),
            originalMessage
        ).queue();
        // plugin.getPlayerChatLog(player).log(originalMessage);
        // plugin.getGlobalChatLog().log(player.getName() + ": " + originalMessage);
    }
}
