package me.justeli.sqlwrapper;

import java.sql.SQLException;

@FunctionalInterface
public interface SQLHandler<T>
{
    void handle (T t)
    throws SQLException;
}
