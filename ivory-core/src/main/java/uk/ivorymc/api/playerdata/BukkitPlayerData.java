package uk.ivorymc.api.playerdata;

import org.bukkit.entity.Player;
import uk.ivorymc.api.storage.YamlFile;
import uk.ivorymc.api.Module;

import java.nio.file.Path;

public class BukkitPlayerData extends YamlFile
{
    private final Module plugin;

    public BukkitPlayerData(Path path, Player player, Module plugin)
    {
        super(path, player.getUniqueId() + ".yml");
        this.plugin = plugin;
    }

    public void saveAsync()
    {
        plugin.async().run(this::save);
    }

    @Override
    public void saveDefaults() {}
}
