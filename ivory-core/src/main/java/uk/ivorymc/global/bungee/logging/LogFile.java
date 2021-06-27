package uk.ivorymc.global.bungee.logging;

import uk.ivorymc.api.storage.DataFile;
import uk.ivorymc.api.utils.Utils;
import uk.ivorymc.global.bungee.IvoryBungee;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class LogFile extends DataFile
{
    private final IvoryBungee plugin;

    public LogFile(Path path, String name, IvoryBungee plugin)
    {
        super(path, name);
        this.plugin = plugin;
    }

    public void log(String line)
    {
        write("[" + Utils.dateString() + "] " + line);
    }

    public void write(String... lines)
    {
        plugin.async(() -> {
            try(BufferedWriter writer = new BufferedWriter(new FileWriter(file, true)))
            {
                for (String line : lines)
                {
                    writer.append(line).append("\n");
                }
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        });
    }

    public List<String> read()
    {
        List<String> lines = new ArrayList<>();
        plugin.async(() -> {
            try(BufferedReader reader = new BufferedReader(new FileReader(file)))
            {
                reader.lines().takeWhile(line -> !line.isEmpty()).forEach(lines::add);
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        });
        return lines;
    }
}
