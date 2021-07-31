package uk.ivorymc.global.bungee.listeners;

import community.leaf.textchain.adventure.TextChain;

import me.justeli.sqlwrapper.SQL;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
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

import java.util.Objects;
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

        String originalMessage = event.getMessage();
        TextChain formattedMessage = format(player, originalMessage);

        Server currentServer = player.getServer();
        String serverName = currentServer.getInfo().getName();
        String[] joindate = {""};
        plugin.getSqlController().sql().query(
            "SELECT join_date FROM player WHERE uuid=?",
                (Object) SQL.uuidToBytes(player.getUniqueId())
        ).select().complete(resultSet -> {
            if (resultSet.next())
            {
                joindate[0] = resultSet.getString("join_date");
            }
        });
        TextChain chatMessage = TextChain.chain()
            .then(getPrefix(player))
            .then(" ")
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
                .then(joindate[0])
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

    private String getPrefix(ProxiedPlayer player)
    {
        // create empty optional for luckperms api as a fallback for if LP isn't loaded
        Optional<LuckPerms> lpOptional = Optional.empty();
        try
        {
            lpOptional = Optional.of(LuckPermsProvider.get());
        }
        catch (IllegalStateException ignored){} // luckperms not loaded

        // try to get prefix, return empty string if LP isn't present
        // require non-null for prefix and lp user object
        // return formatted string (translate colour and format codes)
        return Message.formatted(lpOptional.map(luckPerms -> Objects.requireNonNull(
            Objects.requireNonNull(
                luckPerms.getUserManager().getUser(player.getUniqueId())
            ).getCachedData().getMetaData().getPrefix()
        )).orElse(""));
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
                if (Message.isUrl(word))
                {
                    formattedMessage.then(Message.url(word));
                }
                else
                {
                    switch (word.charAt(0))
                    {
                        case '#' -> {
                            final int[] count = {0};
                            plugin.getSqlController().sql()
                                .query(
                                    "INSERT INTO hashtags (tag, count) VALUES(?, ?) ON DUPLICATE KEY UPDATE count=count+1",
                                    word, 1
                                ).complete();
                            plugin.getSqlController().sql()
                                .query("SELECT count FROM hashtags WHERE tag=?", word).select().complete(
                                      resultSet -> {
                                          if (resultSet.next())
                                          {
                                              count[0] += resultSet.getInt("count");
                                          }
                                      }
                                );
                            formattedMessage.then(Message.tag(word, count[0]));
                        }
                        case '@' -> formattedMessage.then(Message.ping(word, true, Optional.of(plugin)));
                        default -> formattedMessage.then(word)
                                .color(NamedTextColor.WHITE)
                                .tooltip("Click to send a message")
                                .suggest("/msg " + player.getName() + " ");
                    }
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
