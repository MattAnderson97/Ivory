package uk.ivorymc.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import community.leaf.tasks.bukkit.BukkitTaskSource;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

public abstract class Module extends JavaPlugin implements Listener, BukkitTaskSource, PluginMessageListener
{
    protected Registry registry;

    @Override
    public void onEnable()
    {
        // plugin messaging setup
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "ivory:messaging");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "ivory:messaging", this);
        // plugin setup
        this.registry = new Registry(this);
        registerCommands();
        registerEvents();
    }

    @Override
    public void onDisable()
    {
        //make sure to unregister the registered channels in case of a reload
        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        this.getServer().getMessenger().unregisterIncomingPluginChannel(this);
    }

    @Override
    public @NotNull Plugin plugin()
    {
        return this;
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte[] message){}

    protected abstract void registerCommands();
    protected abstract void registerEvents();
}
