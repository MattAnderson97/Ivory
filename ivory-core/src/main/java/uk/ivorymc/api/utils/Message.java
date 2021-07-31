package uk.ivorymc.api.utils;

import community.leaf.textchain.adventure.TextChain;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.bukkit.entity.Player;
import uk.ivorymc.global.bungee.IvoryBungee;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

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

    public static TextChain tag(String tag, int count)
    {
        return TextChain.chain()
            .then(tag)
                .color(NamedTextColor.AQUA)
                .italic()
                .tooltip(
                    TextChain.chain()
                        .then("Times used: ")
                        .then(String.valueOf(count))
                        .color(TextColor.color(0x018786))
                );
    }

    public static boolean isUrl(String url)
    {
        return Pattern.matches("https?://(www\\.)?[-a-zA-Z0-9._]{2,256}\\.[a-z]{2,4}\\b([-a-zA-Z0-9@:%_+.~#?&/=]*)", url);
    }

    public static TextChain url(String url)
    {
        if (!isUrl(url))
        {
            return TextChain.chain().then(url);
        }
        return TextChain.chain()
            .then("<")
                .color(NamedTextColor.DARK_GRAY)
                .italic()
                .link(url)
                .tooltip(url)
            .then(trimUrl(url))
                .color(NamedTextColor.DARK_GRAY)
                .italic()
                .link(url)
                .tooltip(url)
                .underlined()
            .then(">")
                .color(NamedTextColor.DARK_GRAY)
                .italic()
                .link(url)
                .tooltip(url);
    }

    public static String trimUrl(String url)
    {
        if (isUrl(url))
        {
            url = url.replaceFirst("https?://(www\\.)?", "").split("/")[0];
        }
        return url;
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

    public static TextChain getPagesFooter(int pageNo, int size, String command)
    {
        TextChain pre = TextChain.chain(), post = TextChain.chain();
        // footer prefix
        if (pageNo > 1)
        {
            pre.then("<<")
                    .bold()
                    .color(TextColor.color(0x018786))
                    .suggest(command + " 1")
                    .then(" ")
                    .then("<")
                    .bold()
                    .color(TextColor.color(0x03DAC6))
                    .suggest(command + " " + (pageNo - 1))
                    .then(" ");
        }
        else
        {
            pre.then("<< < ").bold().color(NamedTextColor.DARK_GRAY);
        }
        // footer suffix
        if (pageNo < size)
        {
            post.then(" ")
                    .then(">")
                    .bold()
                    .color(TextColor.color(0x03DAC6))
                    .suggest(command + " " + (pageNo + 1))
                    .then(" ")
                    .then(">>")
                    .bold()
                    .color(TextColor.color(0x018786))
                    .suggest(command + " " + size);
        }
        else
        {
            post.then(" > >>").bold().color(NamedTextColor.DARK_GRAY);
        }

        return TextChain.chain()
                .then(pre)
                .then("(")
                    .unformatted()
                    .color(NamedTextColor.WHITE)
                .then(String.valueOf(pageNo))
                    .color(TextColor.color(0x03DAC6))
                .then("/")
                    .color(NamedTextColor.WHITE)
                .then(String.valueOf(size))
                    .color(TextColor.color(0x03DAC6))
                .then(")")
                    .color(NamedTextColor.WHITE)
                .then(post);
    }
}
