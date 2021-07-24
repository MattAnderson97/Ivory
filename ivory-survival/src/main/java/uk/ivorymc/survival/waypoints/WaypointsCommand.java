package uk.ivorymc.survival.waypoints;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.Flag;
import cloud.commandframework.annotations.ProxiedBy;
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
import java.util.Optional;
import java.util.stream.Collectors;

public record WaypointsCommand(Survival survival) implements Command
{
    @CommandMethod("waypoints set <name>")
    @CommandDescription("Set a waypoint")
    @ProxiedBy("wpset")
    public void setWaypoint(
        final @NonNull Player sender,
        final @NonNull @Argument("name") String name,
        @Nullable @Flag(value="player", aliases={"p"}) OfflinePlayer target
    )
    {
        // get the target to send the command to
        Optional<OfflinePlayer> targetOptional = getTarget(sender, target);
        // make sure that there is a target
        if (targetOptional.isEmpty())
        {
            // no target, send error and return
            Message.error("Please provide a player to list waypoints for", "").send(survival.audience(sender));
            return;
        }
        // got a target, update target variable
        target = targetOptional.get();

        // get the sender's location for the waypoint
        Location loc = sender.getLocation();
        // check if the waypoint exists
        if (!survival.getWaypointsController().exists(target, name))
        {
            // doesn't exist, try to create a new waypoint
            if (survival.getWaypointsController().addWaypoint(target, name, loc))
            {
                // waypoint created successfully, send confirmation to player
                TextChain.chain().then("Created waypoint ").then(name).send(survival.audience(sender));
            }
            else
            {
                // failed to create, send error to player
                Message.error(
                    "Failed to create waypoint " + name + ".",
                    "\nIf you believe you got this message in error please contact an administrator."
                ).send(survival.audience(sender));
            }
        }
        else
        {
            // exists, try to update
            if (survival.getWaypointsController().updateWaypoint(target, name, loc))
            {
                // updated successfully, send confirmation to player
                TextChain.chain().then("Updated waypoint ").then(name).send(survival.audience(sender));
            }
            else
            {
                // failed to update, send error to player
                Message.error(
                    "Failed to update waypoint " + name + ".",
                    "\nIf you believe you got this message in error please contact an administrator."
                ).send(survival.audience(sender));
            }
        }
    }

    @CommandMethod("waypoints list [page]")
    @CommandDescription("List waypoints")
    @ProxiedBy("wpls")
    public void listWaypoints(
        final @NonNull CommandSender sender,
        final @Argument(value = "page", defaultValue = "1") int page,
        @Nullable @Flag(value="player", aliases={"p"}) OfflinePlayer target
    )
    {
        // get the target to send the command to
        Optional<OfflinePlayer> targetOptional = getTarget(sender, target);
        // make sure that there is a target
        if (targetOptional.isEmpty())
        {
            // no target, send error and return
            Message.error("Please provide a player to list waypoints for", "").send(survival.audience(sender));
            return;
        }
        target = targetOptional.get();

        // get a list of TextChain lines for the target's waypoints
        List<TextChain> lines = (
            (sender instanceof Player player) ?
                // sender is player, get waypoints sorted by distance and map to TextChain output with distance
                survival.getWaypointsController().getWaypoints(target, player.getLocation())
                        .stream()
                        .map(waypoint -> waypoint.getChain(player))
                // sender is not player, get waypoints supported by name and map to basic TextChain output of name and location
                : survival.getWaypointsController().getWaypoints(target)
                        .stream()
                        .map(Waypoint::getSimpleChain)
        // collect the final stream into a list (List<TextChain>)
        ).collect(Collectors.toList());
        // send the lines to the command sender
        sendWaypoints(lines, page, survival.audience(sender));
    }

    @CommandMethod("waypoints delete <waypoint>")
    @CommandDescription("Delete a waypoint")
    @ProxiedBy("wpdel")
    public void deleteWaypoint(
        final @NonNull CommandSender sender,
        final @NonNull @Argument("waypoint") String waypoint,
        @Nullable @Flag(value="player", aliases={"p"}) OfflinePlayer target
    )
    {
        Optional<OfflinePlayer> targetOptional = getTarget(sender, target);
        if (targetOptional.isEmpty())
        {
            return;
        }
        target = targetOptional.get();

        if (survival.getWaypointsController().deleteWaypoint(target, waypoint))
        {
            TextChain.chain().then("Deleted waypoint ").then(waypoint).send(survival.audience(sender));
        }
    }

    private Optional<OfflinePlayer> getTarget(CommandSender sender, OfflinePlayer target)
    {
        if (sender instanceof ConsoleCommandSender)
        {
            if (target == null)
            {
                return Optional.empty();
            }
        }
        else
        {
            Player player = (Player) sender;
            if (target == null || !player.hasPermission("waypoints.target_other"))
            {
                target = player;
            }
        }
        return Optional.ofNullable(target);
    }

    private void sendWaypoints(List<TextChain> lines, int page, Audience audience)
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
