package uk.ivorymc.api.storage;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class YamlFile extends DataFile
{
    private final ConfigurationProvider provider;
    private Configuration config;

    public YamlFile(Path path, String name)
    {
        super(path, name);
        this.provider = ConfigurationProvider.getProvider(YamlConfiguration.class);
        try
        {
            this.config = provider.load(file);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void save()
    {
        try
        {
            provider.save(config,file);
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

    public Configuration getConfig()
    {
        return config;
    }

    public abstract void saveAsync();
}
