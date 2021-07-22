package uk.ivorymc.survival.waypoints;

import me.justeli.sqlwrapper.SQL;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import uk.ivorymc.survival.Survival;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class WaypointsController
{
    private final Survival survival;
    private final HashMap<UUID, List<Waypoint>> waypointsMap;

    public WaypointsController(Survival survival)
    {
        this.survival = survival;
        waypointsMap = new HashMap<>();
        // getWaypointsFromDB();
    }

    public boolean hasWaypoints(OfflinePlayer player)
    {
        return waypointsMap.containsKey(player.getUniqueId());
    }

    public List<Waypoint> getWaypoints(OfflinePlayer player)
    {
        if (!waypointsMap.containsKey(player.getUniqueId()))
        {
            loadWaypoints(player);
        }
        return (hasWaypoints(player) ?
            waypointsMap.get(player.getUniqueId()) : new ArrayList<>());
    }

    public List<Waypoint> getWaypoints(OfflinePlayer player, Location loc)
    {
        List<Waypoint> list = getWaypoints(player)
            .stream()
            .filter(wp -> Objects.equals(wp.location().getWorld(), loc.getWorld()))
            .sorted(Comparator.comparing(wp -> wp.distance(loc)))
            .collect(Collectors.toList());
        list.addAll(
            getWaypoints(player)
                .stream()
                .filter(waypoint -> !list.contains(waypoint))
                .sorted(Comparator.comparing(Waypoint::name))
                .collect(Collectors.toList())
        );
        return list;
    }

    public boolean addWaypoint(OfflinePlayer player, Waypoint waypoint)
    {
        if (exists(player, waypoint.name()))
        {
            return false;
        }
        List<Waypoint> waypoints = getWaypoints(player);
        waypoints.add(waypoint);
        waypointsMap.put(player.getUniqueId(), waypoints);
        return true;
    }

    public boolean addWaypoint(OfflinePlayer owner, String name, Location loc)
    {
        return addWaypoint(owner, new Waypoint(name, owner, loc));
    }

    public void deleteWaypoint(OfflinePlayer player, Waypoint waypoint)
    {
        waypointsMap.remove(player.getUniqueId());
        survival.getSqlController().sql().query("DELETE FROM waypoints WHERE name = ?", waypoint.name()).queue();
    }

    public boolean updateWaypoint(OfflinePlayer player, String name, Location loc)
    {
        // create waypoint if it doesn't exist
        if (!exists(player, name))
        {
            return addWaypoint(player, name, loc);
        }

        // get player's waypoints
        UUID playerUUID = player.getUniqueId();
        List<Waypoint> waypoints = getWaypoints(player);
        // get optional of player's waypoint (may be empty if waypoint still isn't set somehow)
        Optional<Waypoint> wpOptional = getWaypoint(player, name);
        // make sure waypoint optional isn't empty
        if (wpOptional.isEmpty())
        {
            return false;
        }
        // get the waypoint
        Waypoint wp = wpOptional.get();
        // update the waypoint in the list
        waypoints.set(waypoints.indexOf(wp), wp.update(name, loc));
        // update the list in the map (idk if this is actually needed since lists in java are referenced rather than passed by value)
        waypointsMap.put(playerUUID, waypoints);
        // waypoint successfully updated
        return true;
    }

    public Optional<Waypoint> getWaypoint(OfflinePlayer player, String name)
    {
        // check if waypoint is set
        if (!exists(player, name))
        {
            // waypoint not set, return empty optional
            return Optional.empty();
        }
        // try to get the waypoint as an optional (may be empty if waypoint still can't be found)
        return getWaypoints(player).stream().filter(waypoint -> waypoint.name().equalsIgnoreCase(name)).findFirst();
    }

    public boolean exists(OfflinePlayer player, String name)
    {
        return getWaypoints(player).stream().anyMatch(waypoint -> waypoint.name().equalsIgnoreCase(name));
    }

    public void setCompass(Player player, OfflinePlayer owner, String name)
    {
        if (exists(owner, name))
        {
            Optional<Waypoint> wpOptional = getWaypoint(owner, name);
            wpOptional.ifPresent(waypoint -> setCompass(player, waypoint));
        }
    }

    public void setCompass(Player player, Waypoint waypoint)
    {
        player.setCompassTarget(waypoint.location());
    }

    public void resetCompass(Player player)
    {
        player.setCompassTarget(
            Optional.ofNullable(player.getBedSpawnLocation()).orElseGet(Bukkit.getWorlds().get(0)::getSpawnLocation)
        );
    }

    public void saveWaypoint(Waypoint waypoint)
    {
        Location loc = waypoint.location();
        if (existsInDB(waypoint))
        {
            updateWaypointInDB(waypoint);
            return;
        }
        survival.getSqlController().sql().query(
            "INSERT INTO waypoints(name, owner_uuid, world, x, y, z) VALUES (?,?,?,?,?,?) " +
                "ON DUPLICATE KEY UPDATE " +
                "owner_uuid = VALUES(owner_uuid), " +
                "world = VALUES(world), " +
                "x = VALUES(x), " +
                "y = VALUES(y), " +
                "z = VALUES(z)",
            waypoint.name(),
            SQL.uuidToBytes(waypoint.owner().getUniqueId()),
            waypoint.getWorld().getName(),
            loc.getX(),
            loc.getY(),
            loc.getZ()
        ).complete();
    }

    public void saveWaypoints()
    {
        waypointsMap.values().forEach(waypoints -> waypoints.forEach(this::saveWaypoint));
    }

    public void saveWaypoints(OfflinePlayer player)
    {
        waypointsMap.get(player.getUniqueId()).forEach(this::saveWaypoint);
        waypointsMap.remove(player.getUniqueId());
    }

    public void setWaypoints(OfflinePlayer player, List<Waypoint> waypoints)
    {
        waypointsMap.put(player.getUniqueId(), waypoints);
    }

    public void clearMap()
    {
        waypointsMap.clear();
    }

    public void loadWaypoints(OfflinePlayer player)
    {
        survival.getSqlController().sql().query(
            "SELECT * FROM waypoints WHERE owner_uuid=?",
            (Object) SQL.uuidToBytes(player.getUniqueId())
        ).select().complete(resultSet -> {
            List<Waypoint> waypoints = new ArrayList<>();
            while(resultSet.next())
            {
                UUID ownerUUID = SQL.bytesToUuid(resultSet.getBytes("owner_uuid"));
                OfflinePlayer owner = Bukkit.getOfflinePlayer(ownerUUID);
                String name = resultSet.getString("name");
                double x = resultSet.getDouble("x");
                double y = resultSet.getDouble("y");
                double z = resultSet.getDouble("z");
                World world = Bukkit.getWorld(resultSet.getString("world"));
                waypoints.add(new Waypoint(name, owner, new Location(world, x, y, z)));
            }
            waypointsMap.put(player.getUniqueId(), waypoints);
        });
    }

    private boolean existsInDB(Waypoint waypoint)
    {
        final boolean[] exists = new boolean[1];
        survival.getSqlController().sql().query(
            "SELECT EXISTS(SELECT * FROM waypoints WHERE owner_uuid=? AND name=?) AS 'exists'",
            SQL.uuidToBytes(waypoint.owner().getUniqueId()),
            waypoint.name()
        ).select().complete(results -> exists[0] = results.next() && results.getBoolean("exists"));
        return exists[0];
    }

    private void updateWaypointInDB(Waypoint waypoint)
    {
        Location loc = waypoint.location();
        survival.getSqlController().sql().query(
            "UPDATE waypoints SET world=?, x=?, y=?, z=? WHERE owner_uuid=? AND name=?",
            waypoint.getWorld().getName(),
            loc.getX(),
            loc.getY(),
            loc.getZ(),
            SQL.uuidToBytes(waypoint.owner().getUniqueId()),
            waypoint.name()
        ).queue();
    }

    private void getWaypointsFromDB()
    {
        survival.getSqlController().sql().query("SELECT * FROM waypoints").select().queue(
            result -> {
                while (result.next())
                {
                    String name = result.getString("name");
                    UUID ownerUUID = SQL.bytesToUuid(result.getBytes("owner_uuid"));
                    OfflinePlayer owner = Bukkit.getOfflinePlayer(ownerUUID);
                    World world = Bukkit.getWorld(result.getString("world"));
                    double x = result.getDouble("x");
                    double y = result.getDouble("y");
                    double z = result.getDouble("z");
                    Location loc = new Location(world, x, y, z);
                    Waypoint waypoint = new Waypoint(name, owner, loc);
                    List<Waypoint> waypoints = hasWaypoints(owner) ? getWaypoints(owner) : new ArrayList<>();
                    waypoints.add(waypoint);
                    setWaypoints(owner, waypoints);
                }
            }
        );
    }
}
