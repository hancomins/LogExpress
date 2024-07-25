package com.hancomins.logexpress.util;


import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

public class StandardCharsets {
    public static final Charset US_ASCII;
    public static final Charset ISO_8859_1;
    public static final Charset UTF_8;
    public static final Charset UTF_16BE;
    public static final Charset UTF_16LE;
    public static final Charset UTF_16;

    private StandardCharsets() {
        throw new AssertionError("No java.nio.charset.StandardCharsets instances for you!");
    }

    static {
        US_ASCII = forName("US-ASCII");
        ISO_8859_1 = forName("ISO-8859-1");
        UTF_8 = forName("UTF-8");
        UTF_16BE = forName("UTF-16BE");
        UTF_16LE = forName("UTF-16LE");
        UTF_16 = forName("UTF-16");
    }

    private static Charset forName(String charsetName) {
        try {
            return Charset.forName(charsetName);
        } catch (UnsupportedCharsetException ignored) {
            return null;
        }
    }
}
