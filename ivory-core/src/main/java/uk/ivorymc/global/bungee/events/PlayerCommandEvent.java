package uk.ivorymc.global.bungee.events;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Cancellable;
import net.md_5.bungee.api.plugin.Event;

public class PlayerCommandEvent extends Event implements Cancellable
{
    private boolean cancelled;
    private final String command;
    private final ProxiedPlayer player;

    public PlayerCommandEvent(ProxiedPlayer player, String command)
    {
        this.player = player;
        this.command = command;
    }

    @Override
    public boolean isCancelled()
    {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled)
    {
        this.cancelled = cancelled;
    }

    public ProxiedPlayer getPlayer() { return player; }
    public String getCommand() { return command; }
}
