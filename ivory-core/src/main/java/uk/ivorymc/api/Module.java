package uk.ivorymc.api;

import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import community.leaf.tasks.bukkit.BukkitTaskSource;
import org.jetbrains.annotations.NotNull;

public abstract class Module extends JavaPlugin implements Listener, BukkitTaskSource
{
    protected Registry registry;

    @Override
    public void onEnable()
    {
        this.registry = new Registry(this);
        registerCommands();
        registerEvents();
    }

    @Override
    public @NotNull Plugin plugin()
    {
        return this;
    }

    protected abstract void registerCommands();
    protected abstract void registerEvents();
}
