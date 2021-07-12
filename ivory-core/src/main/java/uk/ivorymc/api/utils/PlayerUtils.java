package uk.ivorymc.api.utils;

import me.justeli.sqlwrapper.SQL;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import uk.ivorymc.api.storage.SQLController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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

    public static Optional<UUID> getPlayerUUIDFromDB(String name, SQLController sqlController)
    {
        final Optional<UUID>[] target = new Optional[]{Optional.empty()};
        sqlController.sql().query(
            "SELECT uuid FROM player WHERE name LIKE ? LIMIT 1;",
            name + "%"
        ).select().complete(result -> {
            if (result.next())
            {
                target[0] = Optional.of(SQL.bytesToUuid(result.getBytes("uuid")));
            }
        });
        return target[0];
    }

    public static List<UUID> getUUIDsFromDB(SQLController sqlController)
    {
        List<UUID> uuids = new ArrayList<>();
        sqlController.sql().query(
            "SELECT uuid FROM player;"
        ).select().complete(result -> {
            if (result.next())
            {
                uuids.add(SQL.bytesToUuid(result.getBytes("uuid")));
            }
        });
        return uuids;
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
