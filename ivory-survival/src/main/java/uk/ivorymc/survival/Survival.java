package uk.ivorymc.survival;

import uk.ivorymc.api.Module;
import uk.ivorymc.survival.listeners.JoinListener;

public class Survival extends Module
{
    @Override
    public void onEnable() { super.onEnable(); }

    @Override
    public void onDisable() { super.onDisable(); }

    @Override
    protected void registerCommands()
    {

    }

    @Override
    protected void registerEvents()
    {
        registry.registerEvents(new JoinListener());
    }
}
