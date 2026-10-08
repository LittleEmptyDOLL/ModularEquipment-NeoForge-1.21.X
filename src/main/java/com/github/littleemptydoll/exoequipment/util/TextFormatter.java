package com.github.littleemptydoll.exoequipment.util;

import net.minecraft.client.gui.Font;

/** Cuts visible UI text to a pixel width, preserving room for an ellipsis. */
public final class TextFormatter {
    private static final String ELLIPSIS = "...";

    private TextFormatter() {}

    public static String truncate(Font font, String text, int maxWidth) {
        if (maxWidth <= 0) return "";
        if (font.width(text) <= maxWidth) return text;
        int available = maxWidth - font.width(ELLIPSIS);
        if (available < 0) return "";
        return font.plainSubstrByWidth(text, available) + ELLIPSIS;
    }
}
