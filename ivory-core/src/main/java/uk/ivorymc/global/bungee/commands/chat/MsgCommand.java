package uk.ivorymc.global.bungee.commands.chat;

import community.leaf.textchain.adventure.TextChain;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import uk.ivorymc.api.MessageType;
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
        super("message", "", "msg", "m", "whisper", "w", "pm");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender commandSender, String[] args)
    {
        if (args.length < 2)
        {
            Message.error("Not enough arguments!", "/message <player> message")
                .send(plugin.adventure().sender(commandSender));
            return;
        }

        if (!(commandSender instanceof ProxiedPlayer sender))
        {
            Message.error("This command is only executable by players", "")
                .send(plugin.adventure().sender(commandSender));
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

        send(target, sender, getFullPre("FROM", sender), message);
        send(sender, target, getFullPre("TO", target), message);

        plugin.addReply(target, sender);
        plugin.addReply(sender, target);
    }

    private TextChain getFullPre(String pre, ProxiedPlayer sender)
    {
        return TextChain.chain()
            .then(pre)
                .color(TextColor.color(0x03DAC6))
                .bold()
                .italic()
            .then(" ")
                .unformatted()
            .then(sender.getDisplayName())
                .color(NamedTextColor.GRAY)
            .then(": ")
                .color(NamedTextColor.WHITE);
    }

    private TextChain getShortPre()
    {
        return TextChain.chain()
            .then("  ")
                .color(TextColor.color(0x03DAC6))
                .strikethrough()
                .bold()
                .italic()
            .then(">")
                .color(TextColor.color(0x03DAC6))
                .strikethrough(false)
                .bold()
                .italic()
            .then(" ")
                .unformatted();
    }

    private void send(ProxiedPlayer p1, ProxiedPlayer p2, TextChain fullPre, String message)
    {
        plugin.getPlayer(p1).ifPresent(
            proxiedPlayer -> {
                TextChain pre;
                Optional<ProxiedPlayer> lastSender = proxiedPlayer.getLastSender();
                if (lastSender.isPresent())
                {
                    if (lastSender.get().equals(p2) && proxiedPlayer.getLastMessageType() == MessageType.PRIVATE)
                    {
                        pre = getShortPre();
                    }
                    else
                    {
                        pre = fullPre;
                    }
                }
                else
                {
                    pre = fullPre;
                }

                proxiedPlayer.sendMessage(
                    TextChain.chain().then(pre).then(message),
                    p2,
                    MessageType.PRIVATE
                );

            }
        );
    }
}
