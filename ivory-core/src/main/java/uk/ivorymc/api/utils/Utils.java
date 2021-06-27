package uk.ivorymc.api.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Utils
{
    public static String dateString()
    {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}
