package uk.ivorymc.api.storage;


import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.nio.file.Path;

public abstract class YamlFile extends DataFile
{
    private FileConfiguration config;

    public YamlFile(Path path, String name)
    {
        super(path, name);
        this.config = new YamlConfiguration();
        try
        {
            config.load(file);
        }
        catch (IOException | InvalidConfigurationException e)
        {
            e.printStackTrace();
        }
        saveDefaults();
    }

    public void save()
    {
        try
        {
            config.save(file);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void write(String key, Object value)
    {
        config.set(key, value);
        saveAsync();
    }

    public boolean isSet(String key)
    {
        return config.contains(key);
    }

    public FileConfiguration getConfig()
    {
        return config;
    }

    public abstract void saveAsync();

    public abstract void saveDefaults();
}
