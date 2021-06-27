package uk.ivorymc.api;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class Registry
{
    private final JavaPlugin plugin;

    public Registry(JavaPlugin plugin)
    {
        this.plugin = plugin;
    }

    public void registerCommands() {}

    public void registerEvents(Listener listener)
    {
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
    }
}