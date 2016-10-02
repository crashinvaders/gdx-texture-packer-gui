package com.crashinvaders.texturepackergui.utils;

import com.badlogic.gdx.utils.Array;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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

    public static String obtainStackTrace(Throwable e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
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

    /**
     * Counts number of set bits
     */
    public static int getSetBits(int i) {
        // Bit count algorithm from here http://stackoverflow.com/a/109025/3802890
        i = i - ((i >>> 1) & 0x55555555);
        i = (i & 0x33333333) + ((i >>> 2) & 0x33333333);
        return (((i + (i >>> 4)) & 0x0F0F0F0F) * 0x01010101) >>> 24;
    }

    /**
     * @param dateString in dd.MM.yyyy format
     */
    public static Date dateFromString(String dateString) {
        try {
            return new SimpleDateFormat("dd.MM.yyyy").parse(dateString);
        } catch (ParseException e) {
            // Should never happen
            throw new RuntimeException(e);
        }
    }

    /**
     * Simple XOR encryption
     * @see <a href="https://github.com/KyleBanks/XOREncryption/blob/master/Java%20(Android%20compatible)/XOREncryption.java">Sources on GitHub</a>
     */
    public static String xor(String key, String input) {
        StringBuilder output = new StringBuilder();

        for(int i = 0; i < input.length(); i++) {
            output.append((char) (input.charAt(i) ^ key.charAt(i % key.length())));
        }

        return output.toString();
    }

    private static int textWidth(String str) {
        return str.length() - str.replaceAll("[^iIl1\\.,']", "").length() / 2;
    }

    /**
     * From <a href="http://stackoverflow.com/a/3657496/3802890">StackOverflow</a>
     */
    public static String ellipsize(String text, int max) {
        if (textWidth(text) <= max) return text;

        // Start by chopping off at the word before max
        // This is an over-approximation due to thin-characters...
        int end = text.lastIndexOf(' ', max - 3);

        // Just one long word. Chop it off.
        if (end == -1) return text.substring(0, max-3) + "...";

        // Step forward as long as textWidth allows.
        int newEnd = end;
        do {
            end = newEnd;
            newEnd = text.indexOf(' ', end + 1);

            // No more spaces.
            if (newEnd == -1)
                newEnd = text.length();

        } while (textWidth(text.substring(0, newEnd) + "...") < max);

        return text.substring(0, end) + "...";
    }
}
