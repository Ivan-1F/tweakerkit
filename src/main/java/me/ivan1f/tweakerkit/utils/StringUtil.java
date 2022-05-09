package me.ivan1f.tweakerkit.utils;

import net.minecraft.util.Formatting;

public class StringUtil {
    public static String removeFormattingCode(String string) {
        return Formatting.strip(string);
    }
}
