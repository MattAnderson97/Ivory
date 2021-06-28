package me.justeli.sqlwrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckReturnValue;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SQL
{
    private final HikariFactory hikariFactory;
    private final Logger logger;

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();

    static
    {
        EXECUTOR_SERVICE.submit(() -> Thread.currentThread().setName("sql-thread"));
    }

    public static SQL open (String database, String username, String password)
    {
        return new SQL(database, "localhost", 3306, username, password);
    }

    public static SQL open (String database, String host, int port, String username, String password)
    {
        return new SQL(database, host, port, username, password);
    }

    private SQL (String database, String host, int port, String username, String password)
    {
        long start = System.currentTimeMillis();

        this.hikariFactory = new HikariFactory(database, host, port, username, password);
        this.logger = LoggerFactory.getLogger(database + "-hikari");

        try (Connection connection = this.hikariFactory.connection())
        {
            String url = connection.getMetaData().getURL();
            logger.info("Opened SQL-connection on '{}' in {}ms.", url, System.currentTimeMillis() - start);
        }
        catch (SQLException exception)
        {
            logger.info("Failed to open SQL-connection on database '{}'.", database);
            error(exception);
        }
    }

    @CheckReturnValue
    public SQLQuery query (String query, Object... replacements)
    {
        return new SQLQuery(this, query, replacements);
    }

    public void close ()
    {
        this.hikariFactory.close();
    }

    void async (Runnable runnable)
    {
        EXECUTOR_SERVICE.submit(runnable);
    }

    void error (Exception exception)
    {
        logger.error(exception.getMessage());

        Throwable throwable = exception.getCause();
        if (throwable == null || throwable.getMessage() == null)
            return;

        logger.error(exception.getCause().getMessage());
    }

    int executeUpdate (String query, Object... replacements)
    {
        try (Connection connection = this.hikariFactory.connection();
             PreparedStatement statement = connection.prepareStatement(query))
        {
            replace(statement, replacements);
            return statement.executeUpdate();
        }
        catch (SQLException exception)
        {
            error(exception);
            return 0;
        }
    }

    ResultSet executeQuery (String query, Object... replacements)
    {
        try (Connection connection = this.hikariFactory.connection();
             PreparedStatement statement = connection.prepareStatement(query))
        {
            replace(statement, replacements);
            return statement.executeQuery();
        }
        catch (SQLException exception)
        {
            error(exception);
            return null;
        }
    }

    private void replace (PreparedStatement statement, Object... replacements)
    throws SQLException
    {
        int i = 1;
        for (Object replacement : replacements)
        {
            if (replacement == null)
            {
                statement.setNull(i, java.sql.Types.NULL);
            }
            else if (replacement instanceof UUID uuid)
            {
                statement.setObject(i, uuidToBytes(uuid));
            }
            else
            {
                statement.setObject(i, replacement);
            }
            i++;
        }
    }

    public static UUID bytesToUuid (byte[] bytes)
    {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        long firstLong = bb.getLong();
        long secondLong = bb.getLong();
        return new UUID(firstLong, secondLong);
    }

    public static byte[] uuidToBytes (UUID uuid)
    {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }
}
