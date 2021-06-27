package uk.ivorymc.global.bungee.commands.chat;

import community.leaf.textchain.adventure.TextChain;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import uk.ivorymc.api.utils.Message;
import uk.ivorymc.global.bungee.IvoryBungee;
import uk.ivorymc.global.bungee.commands.templates.PlayerCommand;

import java.util.Optional;
import java.util.UUID;

public class ReplyCommand extends PlayerCommand
{
    public ReplyCommand(IvoryBungee plugin)
    {
        super("reply", plugin);
    }

    @Override
    public void run(ProxiedPlayer player, String[] args)
    {
        Optional<String> targetOptional = plugin.getReply(player);
        if (targetOptional.isEmpty())
        {
            Message.error("You have no one to reply to", "").send(plugin.adventure().player(player));
            return;
        }
        ProxiedPlayer target = plugin.getProxy().getPlayer(UUID.fromString(targetOptional.get()));

        String message = String.join(" ", args);

        TextChain.chain()
            .then("FROM")
                .color(TextColor.color(0x03DAC6))
                .bold()
                .italic()
            .then(" ")
                .unformatted()
            .then(player.getDisplayName())
                .color(NamedTextColor.GRAY)
            .then(": ")
                .color(NamedTextColor.WHITE)
            .then(message)
                .tooltip("Click to reply")
            .suggest("/msg " + player.getName())
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
            .send(plugin.adventure().player(player));

        plugin.addReply(target, player);
        plugin.addReply(player, target);
    }
}