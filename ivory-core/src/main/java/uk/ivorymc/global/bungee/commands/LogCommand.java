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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class LogCommand extends Command
{
    private final IvoryBungee plugin;

    public LogCommand(IvoryBungee plugin)
    {
        super("log", "staff.logs");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        // check for right amount of args
        if (args.length < 2)
        {
            Message.error(
                "Not enough arguments", "/log <log-type> <player|global> [<page>]"
            ).send(plugin.adventure().sender(sender));
            return;
        }

        // get log type
        String logType = args[0];
        // check if log type is valid
        if (!(logType.equalsIgnoreCase("chat")
            || logType.equalsIgnoreCase("cmd")
            || logType.equalsIgnoreCase("command")
        ))
        {
            Message.error("Invalid log type", "/log <chat|cmd> <player|global> [<page>]")
                .send(plugin.adventure().sender(sender));
            return;
        }
        // update log type
        logType = (logType.equalsIgnoreCase("chat")) ? "chat" : "command";

        // get target argument
        String targetName = args[1];
        // check if command is getting global logs
        boolean isGlobal = targetName.equalsIgnoreCase("global");
        // try to get target from target name argument
        Optional<ProxiedPlayer> targetOptional = PlayerUtils.getProxiedPlayer(targetName);
        // player not found and not global
        if (targetOptional.isEmpty() && !isGlobal)
        {
            Message.error("Player not found", targetName).send(plugin.adventure().sender(sender));
            return;
        }

        // get actual target name
        String target = (isGlobal ? "global" : targetOptional.get().getName());

        // get logs
        List<List<TextChain>> pages = new ArrayList<>();
        switch(logType)
        {
            // get chat logs
            case "chat" -> pages.addAll(
                getLogs("chat", targetOptional, isGlobal)
            );
            // get command logs
            case "command" -> pages.addAll(
                getLogs("command", targetOptional, isGlobal)
            );
        }

        // loop through logs if returned
        if (!pages.isEmpty())
        {
            // get page to output, default to 1 if none provided or invalid
            int pageNo = 1;
            // check if page number was provided in command
            if (args.length >= 3)
            {
                // try to get page number
                try
                {
                    // ensure page number is an integer
                   pageNo = Integer.parseInt(args[2]);
                   // bound to size of pages list
                   if (pageNo > pages.size())
                   {
                       pageNo = pages.size();
                   }
                   // set lower bound to 1
                   if (pageNo < 1)
                   {
                       pageNo = 1;
                   }
                }
                catch(NumberFormatException ignored){}
            }

            // send log title
            TextChain.chain()
                .then("Logs ")
                    .color(NamedTextColor.WHITE)
                    .bold()
                .then(">")
                    .color(TextColor.color(0x018786))
                .then("> ")
                    .color(TextColor.color(0x03DAC6))
                .then(logType)
                    .color(NamedTextColor.WHITE)
                .then(" (")
                .then(target)
                    .color(TextColor.color(0x03DAC6))
                .then(")")
                    .color(NamedTextColor.WHITE)
                    .send(plugin.adventure().sender(sender));

            // get page from list
            List<TextChain> page = pages.get(pageNo - 1);
            // send page
            page.forEach(line -> line.send(plugin.adventure().sender(sender)));
            // send footer
            Message.getPagesFooter(pageNo, pages.size(), "/log " + logType).send(plugin.adventure().sender(sender));
        }
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private List<List<TextChain>> getLogs(String logType, Optional<ProxiedPlayer> targetOptional, boolean global)
    {
        List<List<TextChain>> lines = new ArrayList<>();
        if (global)
        {
            plugin.getSqlController().sql().query(
                "SELECT * FROM " + logType + "_logs"
            ).select().complete(resultSet ->
                lines.addAll(parseResults(resultSet,true))
            );
        }
        else
        {
            if (targetOptional.isEmpty())
            {
                return Collections.emptyList();
            }
            plugin.getSqlController().sql().query(
                "SELECT * FROM " + logType + "_logs WHERE player_uuid = ? ORDER BY date DESC",
                new Object[]{SQL.uuidToBytes(targetOptional.get().getUniqueId())}
            ).select().complete(resultSet ->
                lines.addAll(parseResults(resultSet, false))
            );
        }
        return lines;
    }

    private List<List<TextChain>> parseResults(ResultSet results, boolean global)
    {
        List<TextChain> lines = new ArrayList<>();
        try
        {
            while(results.next())
            {
                String timestamp = results.getTimestamp("date").toString();
                String message = results.getString("message");

                if (global)
                {
                    UUID playerUUID = SQL.bytesToUuid(results.getBytes("player_uuid"));
                    ProxiedPlayer player = plugin.getProxy().getPlayer(playerUUID);

                    lines.add(TextChain.chain()
                        .then("[")
                            .color(TextColor.color(0x018786))
                        .then(timestamp)
                            .color(TextColor.color(0x03DAC6))
                        .then("] ")
                            .color(TextColor.color(0x018786))
                        .then(player.getName())
                            .color(NamedTextColor.AQUA)
                        .then(": ")
                        .then(message)
                    );
                }
                else
                {
                    lines.add(TextChain.chain()
                        .then("[")
                            .color(TextColor.color(0x018786))
                        .then(timestamp)
                            .color(TextColor.color(0x03DAC6))
                        .then("] ")
                            .color(TextColor.color(0x018786))
                        .then(message)
                    );
                }
            }
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }

        return Message.paginate(8, lines);

    }
}
