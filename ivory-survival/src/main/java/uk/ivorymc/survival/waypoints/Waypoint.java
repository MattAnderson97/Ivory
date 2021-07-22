package uk.ivorymc.survival.waypoints;

import community.leaf.textchain.adventure.TextChain;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class Waypoint
{
    private final OfflinePlayer owner;

    private String name;
    private Location location;

    public Waypoint(String name, OfflinePlayer owner, Location location)
    {
        this.name = name;
        this.location = location;
        this.owner = owner;
    }

    public String name()
    {
        return name;
    }

    public OfflinePlayer owner()
    {
        return owner;
    }

    public Location location()
    {
        return location;
    }

    public double distance(Location loc2) { return location.distance(loc2); }
    public World getWorld() { return location.getWorld(); }

    public TextChain getLocationChain()
    {
        return TextChain.chain()
            .then(String.valueOf((int) location.getX()))
            .then(", ")
            .then(String.valueOf((int) location.getY()))
            .then(", ")
            .then(String.valueOf((int) location.getZ()));
    }

    public TextChain getSimpleChain()
    {
        return TextChain.chain()
            .then(name)
            .then(" ")
            .then(getLocationChain());
    }

    public TextChain getChain(Player player)
    {
        TextChain chain = TextChain.chain()
                .then(name)
                .then(": ")
                .then(getLocationChain());
        if (!player.getWorld().getName().equalsIgnoreCase(getWorld().getName()))
        {
            return chain
                .color(NamedTextColor.DARK_GRAY)
                .italic()
                .strikethrough();
        }
        return chain
            .then(" (")
            .then(String.valueOf((int) distance(player.getLocation())))
            .then(")");
    }

    public TextChain getLongChain(Player player)
    {
        TextChain chain = TextChain.chain()
            .then(name)
            .nextLine()
            .then("Location: ")
            .then(getLocationChain())
            .nextLine()
            .then("world: ")
            .then(getWorld().getName());
        if (getWorld().equals(player.getWorld()))
        {
            chain = chain.nextLine()
                .then("Distance: ")
                .then(String.valueOf(distance(player.getLocation())));
        }
        return chain;
    }

    public Waypoint update(String name, Location loc)
    {
        this.name = name;
        this.location = loc;
        return this;
    }
}
