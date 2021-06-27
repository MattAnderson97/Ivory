package uk.ivorymc.survival.waypoints;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public record Waypoint(String name, Player owner, Location location)
{
}
