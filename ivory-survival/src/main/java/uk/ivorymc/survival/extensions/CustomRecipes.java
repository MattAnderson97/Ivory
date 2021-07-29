package uk.ivorymc.survival.extensions;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import uk.ivorymc.survival.Survival;

public class CustomRecipes
{
    private final Survival survival;
    private final NamespacedKey key;

    public CustomRecipes(Survival survival)
    {
        this.survival = survival;
        this.key = new NamespacedKey(survival, "ivory");
        rottenFleshToLeather();
        myceliumRecipe();
    }

    private void rottenFleshToLeather()
    {
        ItemStack leather = new ItemStack(Material.LEATHER, 9);
        ShapelessRecipe recipe = new ShapelessRecipe(key, leather).addIngredient(9, Material.ROTTEN_FLESH);
        survival.getServer().addRecipe(recipe);
    }

    private void myceliumRecipe()
    {
        ItemStack mycelium = new ItemStack(Material.MYCELIUM, 1);
        ShapelessRecipe recipe1 = new ShapelessRecipe(key, mycelium)
            .addIngredient(1, Material.BROWN_MUSHROOM)
            .addIngredient(1, Material.RED_MUSHROOM)
            .addIngredient(1, Material.GRASS_BLOCK);
        ShapelessRecipe recipe2 = new ShapelessRecipe(key, mycelium)
            .addIngredient(1, Material.BROWN_MUSHROOM)
            .addIngredient(1, Material.RED_MUSHROOM)
            .addIngredient(1, Material.DIRT);
        survival.getServer().addRecipe(recipe1);
        survival.getServer().addRecipe(recipe2);
    }
}
