package uk.ivorymc.survival.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import uk.ivorymc.survival.Survival;

import java.util.Arrays;
import java.util.Random;

public class JoinListener implements Listener
{
    private final Material[] invalidMaterials = {
        Material.LAVA, Material.WATER, Material.CACTUS, Material.MAGMA_BLOCK, Material.ACACIA_LEAVES, Material.AZALEA_LEAVES,
        Material.BIRCH_LEAVES, Material.OAK_LEAVES, Material.DARK_OAK_LEAVES, Material.JUNGLE_LEAVES, Material.SPRUCE_LEAVES
    };

    private final Survival survival;

    public JoinListener(Survival survival)
    {
        this.survival = survival;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();
        if (!player.hasPlayedBefore()) {
            // get random spawn location
            Location loc;
            do {
                loc = getRandomLocation(player.getWorld());
            } while (!validLocation(loc));
            // teleport player to spawn location
            player.teleport(loc.add(0, 1, 0));
        }
        survival.getWaypointsController().loadWaypoints(player);

    }

    private int getRandomCoord()
    {
        Random rand = new Random();
        // max 5000, min -5000
        // rand.nextInt(max - min) + min
        // 5000 - (-5000) == 5000 + 5000
        // + (-5000) == - 5000
        return rand.nextInt(5000 + 5000) - 5000;
    }

    private Location getRandomLocation(World world)
    {
        int x,z;
        x = getRandomCoord();
        z = getRandomCoord();
        return world.getHighestBlockAt(x,z).getLocation();
    }

    private boolean validLocation(Location loc)
    {
        return loc != null &&
            Arrays.stream(invalidMaterials).noneMatch(material -> material == loc.getBlock().getType()) &&
            !loc.getBlock().isPassable();
    }
}
