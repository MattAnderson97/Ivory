package uk.ivorymc.survival.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import uk.ivorymc.survival.Survival;

public record QuitListener(Survival survival) implements Listener
{
    @EventHandler
    public void on(PlayerQuitEvent event)
    {
        survival.getWaypointsController().saveWaypoints(event.getPlayer());
    }
}
