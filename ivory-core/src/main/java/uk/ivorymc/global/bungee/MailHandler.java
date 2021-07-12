package uk.ivorymc.global.bungee;

import community.leaf.textchain.adventure.TextChain;
import me.justeli.sqlwrapper.SQL;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import uk.ivorymc.api.storage.SQLController;
import uk.ivorymc.api.utils.Message;
import uk.ivorymc.api.utils.PlayerUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MailHandler
{
    private final IvoryBungee plugin;
    private final SQLController sqlController;

    public MailHandler(IvoryBungee plugin)
    {
        this.plugin = plugin;
        this.sqlController = plugin.getSqlController();
    }

    public void sendMail(ProxiedPlayer sender, String targetName, String message)
    {
        Optional<ProxiedPlayer> targetOptional = PlayerUtils.getProxiedPlayer(targetName);
        if (targetOptional.isPresent())
        {
            sendMail(sender, targetOptional.get().getUniqueId(), message);
        }
        else
        {
            Optional<UUID> uuidOptional = PlayerUtils.getPlayerUUIDFromDB(targetName, plugin.getSqlController());
            if (uuidOptional.isPresent())
            {
                sendMail(sender, uuidOptional.get(), message);
            }
            else
            {
                Message.error("Unknown player: ", targetName);
            }
        }
    }

    public void sendMail(ProxiedPlayer sender, UUID recipient_uuid, String message)
    {
        sqlController.sql().query("INSERT INTO mail(sender_uuid, recipient_uuid, message) VALUES(?,?,?)",
            SQL.uuidToBytes(sender.getUniqueId()),
            SQL.uuidToBytes(recipient_uuid),
            message
        ).queue();
    }

    public void readMail(ProxiedPlayer player)
    {
        // check for mail

        // get all mail from the mail table that's for the player or global
        // along with the sender's name from the player table via an inner join
        sqlController.sql().query(
            "SELECT mail.sender_uuid, mail.recipient_uuid, mail.message as message, mail.date as date, player.name as sender_name FROM mail " +
                "JOIN player ON mail.sender_uuid = player.uuid " +
                "WHERE mail.recipient_uuid = ?",
            new Object[]{SQL.uuidToBytes(player.getUniqueId())}
        ).select().queue(result -> {
            List<TextChain> mail = new ArrayList<>();
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy 'at' HH:mm z");
            while (result.next())
            {
                mail.add(
                    TextChain.chain()
                        .then("From ")
                            .color(TextColor.color(0x03DAC6))
                            .bold()
                            .italic()
                        .then(result.getString("sender_name"))
                            .color(NamedTextColor.WHITE)
                        .then(", ")
                        .then(dateFormat.format(result.getTimestamp("date")))
                            .color(NamedTextColor.AQUA)
                        .nextLine()
                        .then(result.getString("message"))
                            .color(NamedTextColor.WHITE)
                );
            }

            if (mail.isEmpty())
            {
                TextChain.chain()
                    .then(">")
                        .color(TextColor.color(0x018786))
                    .then("> ")
                        .color(TextColor.color(0x03DAC6))
                    .then("You have no mail")
                    .send(plugin.adventure().sender(player));
                return;
            }
            mail.forEach(message -> message.send(plugin.adventure().sender(player)));
        });
    }

    public void clearMail(ProxiedPlayer player)
    {
        sqlController.sql().query(
            "DELETE FROM mail WHERE recipient_uuid = ?",
            new Object[]{SQL.uuidToBytes(player.getUniqueId())}
        ).queue();
    }
}
