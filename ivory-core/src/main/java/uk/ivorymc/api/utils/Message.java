package uk.ivorymc.api.utils;

import community.leaf.textchain.adventure.TextChain;
import net.kyori.adventure.text.format.NamedTextColor;

public class Message
{
    public static TextChain error(String message, String details)
    {
        return TextChain.chain()
            .then(message)
                .color(NamedTextColor.DARK_RED)
            .then(details)
                .color(NamedTextColor.RED);
    }
}
