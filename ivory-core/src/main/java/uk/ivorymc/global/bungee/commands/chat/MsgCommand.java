package uk.ivorymc.global.bungee.commands.chat;

import community.leaf.textchain.adventure.TextChain;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import uk.ivorymc.api.utils.Message;
import uk.ivorymc.api.utils.PlayerUtils;
import uk.ivorymc.global.bungee.IvoryBungee;

import java.util.Arrays;
import java.util.Optional;

public class MsgCommand extends Command
{
    private final IvoryBungee plugin;

    public MsgCommand(IvoryBungee plugin)
    {
        super("message");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        if (args.length < 2)
        {
            Message.error("Not enough arguments!", "/message <player> message")
                .send(plugin.adventure().sender(sender));
            return;
        }

        Optional<ProxiedPlayer> targetOptional = PlayerUtils.getProxiedPlayer(args[0]);

        if (targetOptional.isEmpty())
        {
            Message.error("Player not found: ", args[0]);
            return;
        }
        ProxiedPlayer target = targetOptional.get();

        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        String senderName = (sender instanceof ProxiedPlayer player) ? player.getDisplayName() : sender.getName();

        TextChain.chain()
            .then("FROM")
                .color(TextColor.color(0x03DAC6))
                .bold()
                .italic()
            .then(" ")
                .unformatted()
            .then(senderName)
                .color(NamedTextColor.GRAY)
            .then(": ")
                .color(NamedTextColor.WHITE)
            .then(message)
                .tooltip("Click to reply")
                .suggest("/msg " + sender.getName())
            .send(plugin.adventure().player(target));

        TextChain.chain()
            .then("TO")
                .color(TextColor.color(0x03DAC6))
                .bold()
                .italic()
            .then(" ")
                .unformatted()
            .then(target.getDisplayName())
                .color(NamedTextColor.GRAY)
            .then(": ")
                .color(NamedTextColor.WHITE)
            .then(message)
            .send(plugin.adventure().sender(sender));

        if (sender instanceof ProxiedPlayer player)
        {
            plugin.addReply(target, player);
            plugin.addReply(player, target);
        }
    }
}
