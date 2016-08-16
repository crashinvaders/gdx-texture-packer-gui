package com.crashinvaders.texturepackergui.utils;

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
}
