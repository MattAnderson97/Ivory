package me.justeli.sqlwrapper;

import java.sql.SQLException;

public class SQLQuery
{
    private final SQL sql;
    private final String query;
    private final Object[] replacements;

    public SQLQuery (SQL sql, String query, Object... replacements)
    {
        this.sql = sql;
        this.query = query.endsWith(";")? query : query + ";";
        this.replacements = replacements;
    }

    public int complete ()
    {
        return sql.executeUpdate(query, replacements);
    }

    public void complete (SQLHandler<Integer> handler)
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

    public void queue ()
    {
        sql.async(this::complete);
    }

    public void queue (SQLHandler<Integer> handler)
    {
        sql.async(() -> complete(handler));
    }

    public SQLSelect select ()
    {
        return new SQLSelect(sql, query, replacements);
    }
}
