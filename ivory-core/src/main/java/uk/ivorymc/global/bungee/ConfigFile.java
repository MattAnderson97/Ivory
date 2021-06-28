package uk.ivorymc.global.bungee;

import uk.ivorymc.api.storage.YamlFile;

import java.nio.file.Path;

public class ConfigFile extends YamlFile
{
    private final IvoryBungee plugin;

    public ConfigFile(IvoryBungee plugin, Path path, String name)
    {
        super(path, name);
        this.plugin = plugin;
    }

    public void saveAsync()
    {
        plugin.async(this::save);
    }

    public void saveDefaults()
    {
        setDefault("mysql.database", "ivory");
        setDefault("mysql.host", "127.0.0.1");
        setDefault("mysql.port", 3306);
        setDefault("mysql.username", "");
        setDefault("mysql.password", "");
        saveAsync();
    }

    public void setDefault(String key, Object value)
    {
        if (!getConfig().contains(key))
        {
            getConfig().set(key, value);
        }
    }
}
