package uk.ivorymc.global.bungee.commands.chat;

import community.leaf.textchain.adventure.TextChain;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import uk.ivorymc.api.utils.Message;
import uk.ivorymc.global.bungee.IvoryBungee;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrendingCommand extends Command
{
    private final IvoryBungee plugin;

    public TrendingCommand(IvoryBungee plugin)
    {
        super("trending", "");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        Map<String, Integer> trendingMap = new HashMap<>();
        plugin.getSqlController().sql().query(
            "SELECT tag, count FROM hashtags ORDER BY count DESC;"
        ).select().complete(resultSet -> {
            while(resultSet.next())
            {
                trendingMap.put(resultSet.getString("tag"), resultSet.getInt("count"));
            }
        });
        List<TextChain> lines = new ArrayList<>();
        trendingMap.forEach((tag, count) -> lines.add(
            TextChain.chain()
                .then(tag)
                    .color(NamedTextColor.AQUA)
                .then(": ")
                    .color(NamedTextColor.WHITE)
                .then(String.valueOf(count))
        ));
        List<List<TextChain>> pages = Message.paginate(8, lines);

        int pageNo = 1;
        if (args.length >= 1)
        {
            try
            {
                pageNo = Integer.parseInt(args[0]);
                if (pageNo > pages.size())
                {
                    pageNo = pages.size();
                }
                else if (pageNo < 1)
                {
                    pageNo = 1;
                }
            }
            catch(NumberFormatException e)
            {
                e.printStackTrace();
            }
        }

        pages.get(pageNo - 1).forEach(line -> line.send(plugin.adventure().sender(sender)));
        // send footer
        Message.getPagesFooter(pageNo, pages.size(), "/trending").send(plugin.adventure().sender(sender));
    }
}
