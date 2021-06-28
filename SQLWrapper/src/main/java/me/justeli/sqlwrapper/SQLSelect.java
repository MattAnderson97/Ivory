package me.justeli.sqlwrapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLSelect
{
    private final SQL sql;
    private final String query;
    private final Object[] replacements;

    public SQLSelect (SQL sql, String query, Object... replacements)
    {
        this.sql = sql;
        this.query = query;
        this.replacements = replacements;
    }

    public ResultSet complete ()
    {
        return sql.executeQuery(query, replacements);
    }

    public void complete (SQLHandler<ResultSet> handler)
    {
        try
        {
            handler.handle(complete());
        }
        catch (SQLException exception)
        {
            sql.error(exception);
        }
    }

    public void queue (SQLHandler<ResultSet> handler)
    {
        sql.async(() -> complete(handler));
    }
}
