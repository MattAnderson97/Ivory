package uk.ivorymc.survival.waypoints;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.Flag;
import community.leaf.textchain.adventure.TextChain;
import net.kyori.adventure.audience.Audience;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import uk.ivorymc.api.interfaces.Command;
import uk.ivorymc.api.utils.Message;
import uk.ivorymc.survival.Survival;

import java.util.List;
import java.util.stream.Collectors;

public class WaypointsCommand implements Command
{
    private final Survival survival;

    public WaypointsCommand(Survival survival)
    {
        this.survival = survival;
    }

    @CommandMethod("waypoints set <name>")
    @CommandDescription("Set a waypoint")
    public void setWaypoint(
        final @NonNull Player sender,
        final @NonNull @Argument("name") String name,
        @Nullable @Flag("target") OfflinePlayer target
    )
    {
        if (target == null || !sender.hasPermission("waypoints.target_other"))
        {
            target = sender;
        }

        Location loc = sender.getLocation();
        if (!survival.getWaypointsController().exists(target, name))
        {
            if(survival.getWaypointsController().addWaypoint(target, name, loc))
            {
                TextChain.chain().then("Created waypoint ").then(name).send(survival.audience(sender));
            }
        }
        else
        {
            if(survival.getWaypointsController().updateWaypoint(target, name, loc))
            {
                TextChain.chain().then("Updated waypoint ").then(name).send(survival.audience(sender));
            }
        }
    }

    @CommandMethod("waypoints list [page]")
    @CommandDescription("List waypoints")
    public void listWaypoints(
        final @NonNull CommandSender sender,
        final @Argument(value="page", defaultValue="1") int page,
        @Nullable @Flag("target") OfflinePlayer target
    )
    {
        if (sender instanceof ConsoleCommandSender consoleSender)
        {
            if (target == null)
            {
                Message.error("Please provide a player to list waypoints for", "").send(survival.audience(consoleSender));
                return;
            }
            List<TextChain> lines = survival.getWaypointsController().getWaypoints(target)
                                        .stream()
                                        .map(Waypoint::getSimpleChain)
                                        .collect(Collectors.toList());
            sendWaypoints(lines, page, survival.audience(sender));
            return;
        }

        Player player = (Player) sender;

        if (target == null || !sender.hasPermission("waypoints.target_other"))
        {
            target = player;
        }


        // get list of waypoints sorted by distance
        List<Waypoint> waypoints = survival.getWaypointsController().getWaypoints(target, player.getLocation());
        // get text chains for each waypoint
        List<TextChain> lines = waypoints.stream()
                                    .map(waypoint -> waypoint.getChain(player))
                                    .collect(Collectors.toList());
        sendWaypoints(lines, page, survival.audience(player));
    }

    public void sendWaypoints(List<TextChain> lines, int page, Audience audience)
    {
        // check if the list is empty (no waypoints set)
        if (lines.isEmpty())
        {
            TextChain.chain()
                .then("No waypoints set")
                .send(audience);
            return;
        }
        // split the list into pages
        List<List<TextChain>> pages = Message.paginate(8, lines);
        // first page
        if (page <= 1)
        {
            // send lines from the first page
            pages.get(0).forEach(line -> line.send(audience));
        }
        // last page
        else if (page >= pages.size() - 1)
        {
            // send lines from the last page
            pages.get(pages.size() - 1).forEach(line -> line.send(audience));
        }
        // other pages
        else
        {
            // send lines from the specified page
            pages.get(page - 1).forEach(line -> line.send(audience));
        }
    }
}
