package uk.ivorymc.core;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import uk.ivorymc.api.Module;

public class Core extends Module
{
    @Override
    protected void registerCommands()
    {

    }

    @Override
    protected void registerEvents()
    {

    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte[] message)
    {
        if (!channel.equals("ivory:messaging"))
        {
            return;
        }
        ByteArrayDataInput in = ByteStreams.newDataInput( message );
        String subChannel = in.readUTF();
        if (subChannel.equalsIgnoreCase("ping"))
        {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1f, 1f);
        }
    }
}
