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
import uk.ivorymc.api.utils.Message;
import uk.ivorymc.global.bungee.IvoryBungee;
import uk.ivorymc.global.bungee.events.PlayerCommandEvent;

import java.util.Optional;

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
        TextChain formattedMessage = format(player, originalMessage);

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
            .then(formattedMessage);

        plugin.getProxy().getPlayers().forEach(
            loopPlayer -> chatMessage.send(plugin.adventure().player(loopPlayer))
        );

        // write to chat logs
        plugin.getSqlController().sql().query(
            "INSERT INTO chat_logs(message,player_uuid) VALUES(?,?);",
            originalMessage,
            SQL.uuidToBytes(player.getUniqueId())
        ).queue();
    }

    private TextChain format(ProxiedPlayer player, String originalMessage)
    {
        String[] words = originalMessage.split(" ");
        TextChain formattedMessage = TextChain.chain();

        for (int i = 0; i < words.length; i++)
        {
            String word = words[i];
            if (word.length() > 1)
            {
                switch (word.charAt(0))
                {
                    case '#' -> formattedMessage.then(Message.tag(word));
                    case '@' -> formattedMessage.then(Message.ping(word, true, Optional.of(plugin)));
                    default -> formattedMessage.then(word)
                        .color(NamedTextColor.WHITE)
                        .tooltip("Click to send a message")
                        .suggest("/msg " + player.getName() + " ");
                }
            }
            else
            {
                formattedMessage.then(word)
                    .color(NamedTextColor.WHITE)
                    .tooltip("Click to send a message")
                    .suggest("/msg " + player.getName() + " ");
            }
            if (i < words.length - 1)
            {
                formattedMessage.then(" ")
                    .tooltip("Click to send a message")
                    .suggest("/msg " + player.getName() + " ");
            }
        }

        return formattedMessage;
    }
}
