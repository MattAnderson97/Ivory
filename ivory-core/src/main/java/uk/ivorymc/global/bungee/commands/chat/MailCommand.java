package uk.ivorymc.global.bungee.commands.chat;

import community.leaf.textchain.adventure.TextChain;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import uk.ivorymc.api.utils.Message;
import uk.ivorymc.api.utils.PlayerUtils;
import uk.ivorymc.global.bungee.IvoryBungee;
import uk.ivorymc.global.bungee.commands.templates.PlayerCommand;

import java.util.Arrays;

public class MailCommand extends PlayerCommand
{
    public MailCommand(IvoryBungee plugin)
    {
        super(plugin, "mail");
    }

    @Override
    public void run(ProxiedPlayer player, String[] args)
    {
        if (args.length < 1)
        {
            Message.error("Missing argument: ", "/mail <send|read> [player] [message]")
                .send(plugin.adventure().sender(player));
            return;
        }
        switch(args[0].toLowerCase())
        {
            case "send" -> {
                if (args.length < 3)
                {
                    Message.error("Missing arguments: ", "/mail send <player> <message>")
                        .send(plugin.adventure().sender(player));
                    return;
                }
                plugin.getMailHandler().sendMail(player, args[1], String.join(" ", Arrays.copyOfRange(args, 2, args.length)));

                TextChain.chain()
                    .then(">")
                        .color(TextColor.color(0x018786))
                    .then("> ")
                        .color(TextColor.color(0x03DAC6))
                    .then("Sent mail to ")
                        .color(NamedTextColor.WHITE)
                    .then(args[1])
                    .send(plugin.adventure().sender(player));
            }
            case "send-all", "sendall" -> {
                PlayerUtils.getUUIDsFromDB(plugin.getSqlController()).forEach(
                    target -> plugin.getMailHandler().sendMail(
                        player, target, String.join(" ", Arrays.copyOfRange(args, 1, args.length))
                    )
                );

                TextChain.chain()
                    .then(">")
                        .color(TextColor.color(0x018786))
                    .then("> ")
                        .color(TextColor.color(0x03DAC6))
                    .then("Sent mail to everyone")
                        .color(NamedTextColor.WHITE)
                    .send(plugin.adventure().sender(player));
            }
            case "read" -> plugin.getMailHandler().readMail(player);
            case "clear" -> {
                plugin.getMailHandler().clearMail(player);

                TextChain.chain()
                    .then(">")
                        .color(TextColor.color(0x018786))
                    .then("> ")
                        .color(TextColor.color(0x03DAC6))
                    .then("Mail cleared")
                        .color(NamedTextColor.WHITE)
                    .send(plugin.adventure().sender(player));
            }
        }
    }
}
