package fr.lostaria.hytaleclient.utils;

public final class HttpUtils {

    public static String stripTrailingSlash(String s) {
        if (s == null) return "";
        return s.endsWith("/") ? s.substring(0, s.length() - 1) : s;
    }
}
