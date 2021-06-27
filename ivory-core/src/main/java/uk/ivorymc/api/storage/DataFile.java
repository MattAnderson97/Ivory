package uk.ivorymc.api.storage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class DataFile
{
    protected final File file;

    public DataFile(Path path, String name)
    {
        File folder = path.toFile();
        this.file = new File(folder, name);
        createFile();
    }

    protected void createFile()
    {
        if (!file.getParentFile().exists())
        {
            file.getParentFile().mkdirs();
        }

        if (!file.exists())
        {
            try (InputStream input = getClass().getResourceAsStream("/" + file.getName()))
            {
                if (input != null)
                {
                    Files.copy(input, file.toPath());
                }
                else
                {
                    file.createNewFile();
                }
            }
            catch (IOException exception)
            {
                exception.printStackTrace();
            }
        }
    }
}
