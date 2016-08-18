package com.crashinvaders.texturepackergui.utils;

import com.badlogic.gdx.utils.Array;

import java.util.Iterator;

public class CommonUtils {

    public static String fetchMessageStack(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        while (true) {
            if (sb.length() > 0) { sb.append("\n\t"); }
            sb.append(throwable.getMessage());
            if (throwable.getCause() == null || throwable.getCause() == throwable) break;
            throwable = throwable.getCause();
        }
        return sb.toString();
    }

    /**
     * Splits the lines of a string, and trims each line.
     */
    public static Array<String> splitAndTrim(String str) {
        return splitAndTrim(str, "\n");
    }

    /**
     * Splits a string, and trims each segment.
     */
    public static Array<String> splitAndTrim(String str, String regex) {
        Array<String> lines = new Array<>(str.split(regex));
        // Trim each line
        for (int i = 0; i < lines.size; i++) {
            lines.set(i, lines.get(i).trim());
        }
        // Remove empty lines
        for (Iterator<String> iter = lines.iterator(); iter.hasNext();) {
            if (iter.next().isEmpty()) {
                iter.remove();
            }
        }
        return lines;
    }
}
