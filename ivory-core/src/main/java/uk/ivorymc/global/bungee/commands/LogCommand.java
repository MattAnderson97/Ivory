package uk.ivorymc.global.bungee.commands;

import community.leaf.textchain.adventure.TextChain;
import me.justeli.sqlwrapper.SQL;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import uk.ivorymc.api.utils.Message;
import uk.ivorymc.api.utils.PlayerUtils;
import uk.ivorymc.global.bungee.IvoryBungee;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class LogCommand extends Command
{
    private final IvoryBungee plugin;

    public LogCommand(IvoryBungee plugin)
    {
        super("log");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender commandSender, String[] args)
    {
        if (args.length < 2)
        {
            Message.error(
                "Not enough arguments", "/log <log-type> <player|global> [<page>]"
            ).send(plugin.adventure().sender(commandSender));
            return;
        }

        switch(args[0].toLowerCase())
        {
            case "chat" -> getLogs("chat", args[1], args[1].equalsIgnoreCase("global"), commandSender);
            case "command", "cmd" -> getLogs("command", args[1], args[1].equalsIgnoreCase("global"), commandSender);
            default -> Message.error("Invalid log type", "/log <chat|cmd> <player|global> [<page>]")
                            .send(plugin.adventure().sender(commandSender));
        }
    }

    private void getLogs(String logType, String targetName, boolean global, CommandSender sender)
    {
        if (global)
        {
            plugin.getSqlController().sql().query(
                "SELECT * FROM " + logType + "_logs"
            ).select().queue(resultSet ->
                parseResults(resultSet, logType, true, "global", sender)
            );
        }
        else
        {
            Optional<ProxiedPlayer> targetOptional = PlayerUtils.getProxiedPlayer(targetName);
            if (targetOptional.isEmpty())
            {
                Message.error("Player not found", targetName).send(plugin.adventure().sender(sender));
                return;
            }
            plugin.getSqlController().sql().query(
                "SELECT * FROM chat_logs WHERE player_uuid = ?",
                (Object) SQL.uuidToBytes(targetOptional.get().getUniqueId())
            ).select().queue(resultSet ->
                parseResults(resultSet, logType, false, targetOptional.get().getName(), sender)
            );
        }
    }

    private void parseResults(ResultSet results, String logType, boolean global, String targetName, CommandSender sender)
    {
        List<String> lines = new ArrayList<>();
        try
        {
            while(results.next())
            {
                UUID playerUUID = SQL.bytesToUuid(results.getBytes("player_uuid"));
                ProxiedPlayer player = plugin.getProxy().getPlayer(playerUUID);
                String timestamp = results.getTimestamp("date").toString();
                String message = results.getString("message");

                if (global)
                {
                    lines.add("[" + timestamp + "] " + player.getName() + ": " + message);
                }
                else
                {
                    lines.add("[" + timestamp + "] " + message);
                }
            }
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
        TextChain.chain()
            .then("Logs ")
                .color(NamedTextColor.WHITE)
                .bold()
            .then(" >")
                .color(TextColor.color(0x018786))
            .then("> ")
                .color(TextColor.color(0x03DAC6))
            .then(logType)
                .color(NamedTextColor.WHITE)
            .then(" (")
            .then(targetName)
                .color(TextColor.color(0x03DAC6))
            .then(")")
                .color(NamedTextColor.WHITE)
            .send(plugin.adventure().sender(sender));
        lines.forEach(line -> TextChain.chain().then(line).send(plugin.adventure().sender(sender)));
    }
}
