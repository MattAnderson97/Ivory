package uk.ivorymc.api.utils;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

public class PlayerUtils
{
    public static Optional<ProxiedPlayer> getProxiedPlayer(String name)
    {
        String nameLower = name.toLowerCase();
        return ProxyServer.getInstance().getPlayers().stream().filter(
            player -> player.getName().equalsIgnoreCase(nameLower) ||
                player.getName().toLowerCase().startsWith(nameLower) ||
                player.getDisplayName().equalsIgnoreCase(nameLower) ||
                player.getDisplayName().toLowerCase().startsWith(nameLower)
        ).findFirst();
    }

    public static Collection<Player> getOnlinePlayersBukkit()
    {
        return Bukkit.getServer().getOnlinePlayers().stream().map(player -> (Player) player).collect(Collectors.toList());
    }

    public static Optional<Player> getPlayer(String name)
    {
        String nameLower = name.toLowerCase();
        return getOnlinePlayersBukkit().stream().filter(
            player -> player.getName().equalsIgnoreCase(nameLower) ||
                player.getName().toLowerCase().startsWith(nameLower) ||
                player.getDisplayName().equalsIgnoreCase(nameLower) ||
                player.getDisplayName().toLowerCase().startsWith(nameLower)
        ).findFirst();
    }
}
