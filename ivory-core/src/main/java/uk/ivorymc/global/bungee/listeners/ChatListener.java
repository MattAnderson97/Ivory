package uk.ivorymc.global.bungee.listeners;

import community.leaf.textchain.adventure.TextChain;

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

public record ChatListener(IvoryBungee plugin) implements Listener
{
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(ChatEvent event)
    {
        event.setCancelled(true);

        ProxiedPlayer player = (ProxiedPlayer) event.getSender();
        BungeePlayerData playerData = plugin.getPlayerData(player);

        String originalMessage = event.getMessage();

        Server currentServer = player.getServer();
        String serverName = currentServer.getInfo().getName();

        TextChain chatMessage = TextChain.chain()
            .then(player.getDisplayName())
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
        plugin.getPlayerChatLog(player).log(originalMessage);
        plugin.getGlobalChatLog().log(player.getName() + ": " + originalMessage);
    }
}
