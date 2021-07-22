package uk.ivorymc.api.playerdata;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import uk.ivorymc.api.storage.YamlFile;
import uk.ivorymc.global.bungee.IvoryBungee;

import java.nio.file.Path;

public class BungeePlayerData extends YamlFile
{
    private final IvoryBungee plugin;

    public BungeePlayerData(Path path, ProxiedPlayer player, IvoryBungee plugin)
    {
        super(path, player.getUniqueId().toString() + ".yml");
        this.plugin = plugin;
    }

    public void saveAsync()
    {
        plugin.async(this::save);
    }

    @Override
    public void saveDefaults() {}
}
