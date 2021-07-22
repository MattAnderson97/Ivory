package uk.ivorymc.api;

import org.bukkit.event.Listener;
import uk.ivorymc.api.interfaces.Command;

public class Registry
{
    private final Module plugin;

    public Registry(Module plugin)
    {
        this.plugin = plugin;
    }

    public void registerCommands(Command command)
    {
        plugin.getAnnotationParser().parse(command);
    }

    public void registerEvents(Listener listener)
    {
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
    }
}