package uk.ivorymc.survival.listeners;

import community.leaf.textchain.adventure.TextChain;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import uk.ivorymc.survival.Survival;

public class BlockBreakListener implements Listener
{
    private final Survival survival;

    public BlockBreakListener(Survival survival)
    {
        this.survival = survival;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event)
    {
        Player player = event.getPlayer();
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        if (heldItem.getItemMeta() instanceof Damageable damageable)
        {
            int damage = damageable.getDamage();
            int maxDamage = heldItem.getType().getMaxDurability();
            int remaining = (maxDamage - damage) + 1;
            int percentDamage = (damage/maxDamage) * 100;

            if (remaining == 10)
            {
                TextChain.chain()
                    .then("Your current tool only has 10 blocks of durability left")
                    .actionBar(survival.audience(player));
            }
            else if (percentDamage >=90)
            {
                TextChain.chain()
                    .then("Your current tool only has 10% durability left")
                    .actionBar(survival.audience(player));
            }
        }
    }
}
