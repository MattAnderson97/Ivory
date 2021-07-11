package uk.ivorymc.api.storage;

import me.justeli.sqlwrapper.SQL;
import me.justeli.sqlwrapper.SQLQuery;

public class SQLController
{
    private final SQL sql;

    public SQLController(String name, String host, int port, String username, String password)
    {
        sql = SQL.open(name, host, port, username, password);
    }

    public void createTable(String tableName, String... columns)
    {
        SQLQuery query = sql.query(
            "CREATE TABLE IF NOT EXISTS " + tableName + "(" + String.join(",", columns) + ");"
        );
        query.queue();
    }

    public SQL sql() { return sql; }
}
