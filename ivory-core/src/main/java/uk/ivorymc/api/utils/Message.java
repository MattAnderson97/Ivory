package uk.ivorymc.api.utils;

import community.leaf.textchain.adventure.TextChain;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.bukkit.entity.Player;
import uk.ivorymc.global.bungee.IvoryBungee;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Message
{
    public static TextChain error(String message, String details)
    {
        return TextChain.chain()
            .then(message)
                .color(NamedTextColor.DARK_RED)
            .then(details)
                .color(NamedTextColor.RED);
    }

    public static TextChain tag(String tag)
    {
        return TextChain.chain()
            .then(tag)
                .color(NamedTextColor.AQUA)
                .italic();
    }

    public static List<List<TextChain>> paginate(int pageSize, List<TextChain> lines)
    {
        List<List<TextChain>> pages = new ArrayList<>();
        int maxPages = (lines.size() >= pageSize) ? ((int) Math.ceil((double) lines.size() / pageSize)) : 1;

        for (int i = 0; i < maxPages; i++)
        {
            List<TextChain> page = new ArrayList<>();
            int maxItems = Math.min(pageSize, lines.size());
            for (int j = 0; j < maxItems; j++)
            {
                page.add(lines.remove(0));
            }

            pages.add(page);
        }

        return pages;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static TextChain ping(String name, boolean proxied, Optional<IvoryBungee> proxyServer)
    {
        String trimmedName = name.replace("@", "");
        TextChain chain = TextChain.chain().then(name);
        if (proxied)
        {
            Optional<ProxiedPlayer> targetOptional = PlayerUtils.getProxiedPlayer(trimmedName);
            if (targetOptional.isPresent())
            {
                ProxiedPlayer  target = targetOptional.get();
                chain.color(NamedTextColor.GOLD);
                proxyServer.ifPresent(ivoryBungee -> ivoryBungee.sendCustomData(target, "ping"));
            }
        }
        else
        {
            Optional<Player> targetOptional = PlayerUtils.getPlayer(trimmedName);
            if (targetOptional.isPresent())
            {
                chain.color(NamedTextColor.GOLD);
            }
        }
        return chain;
    }

    public static String formatted(String text)
    {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
