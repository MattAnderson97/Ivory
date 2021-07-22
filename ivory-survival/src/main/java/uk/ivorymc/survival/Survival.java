package uk.ivorymc.survival;

import uk.ivorymc.api.Module;
import uk.ivorymc.survival.listeners.JoinListener;
import uk.ivorymc.survival.listeners.QuitListener;
import uk.ivorymc.survival.waypoints.WaypointsCommand;
import uk.ivorymc.survival.waypoints.WaypointsController;

public class Survival extends Module
{
    private WaypointsController waypointsController;

    @Override
    public void onEnable()
    {
        super.onEnable();

        // create waypoints table
        sqlController.createTable(
            "waypoints",
            "id INT NOT NULL AUTO_INCREMENT",
            "owner_uuid BINARY(16) NOT NULL",
            "name VARCHAR(50) NOT NULL",
            "world VARCHAR(50) NOT NULL",
            "x DOUBLE NOT NULL",
            "y DOUBLE NOT NULL",
            "z DOUBLE NOT NULL",
            "PRIMARY KEY (id)"
        );

        // create waypoints controller instance
        waypointsController = new WaypointsController(this);
    }

    @Override
    public void onDisable()
    {
        super.onDisable();
        waypointsController.saveWaypoints();
        waypointsController.clearMap();
    }

    @Override
    protected void registerCommands()
    {
        registry.registerCommands(new WaypointsCommand(this));
    }

    @Override
    protected void registerEvents()
    {
        registry.registerEvents(new JoinListener(this));
        registry.registerEvents(new QuitListener(this));
    }

    public WaypointsController getWaypointsController() { return waypointsController; }
}
