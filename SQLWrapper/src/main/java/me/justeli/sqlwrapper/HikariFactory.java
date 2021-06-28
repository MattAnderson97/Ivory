package me.justeli.sqlwrapper;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

class HikariFactory
{
    private final HikariDataSource hikari;

    HikariFactory (String database, String host, int port, String username, String password)
    {
        HikariConfig config = new HikariConfig();
        config.setPoolName(database + "-hikari");
        config.setDriverClassName("org.mariadb.jdbc.Driver");
        config.setJdbcUrl("jdbc:mariadb://" + host + ":" + port + "/" + database);
        config.setUsername(username);
        config.setPassword(password);
        config.addDataSourceProperty("useUnicode", "true");
        config.addDataSourceProperty("characterEncoding", "utf8");
        config.addDataSourceProperty("useSSL", "false");
        config.addDataSourceProperty("socketTimeout", TimeUnit.SECONDS.toMillis(30));
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(10);
        config.setMaxLifetime(1800000);
        config.setKeepaliveTime(0);
        config.setConnectionTimeout(5000);
        config.setInitializationFailTimeout(-1);

        this.hikari = new HikariDataSource(config);
    }

    public void close ()
    {
        if (this.hikari == null)
            return;

        this.hikari.close();
    }

    public Connection connection ()
    throws SQLException
    {
        if (this.hikari == null)
        {
            throw new SQLException("Unable to get a connection from Hikari pool.");
        }

        return this.hikari.getConnection();
    }
}