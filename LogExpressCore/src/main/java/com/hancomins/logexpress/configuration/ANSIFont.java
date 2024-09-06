package com.hancomins.logexpress.configuration;

public enum ANSIFont implements ANSIStyle {
    BOLD("1", "22"),
    FAINT("2", "22"),
    ITALIC("3", "23"),
    UNDERLINE("4", "24"),
    REVERSE("7", "27"),
    STRIKE("9", "29");


    private final String code;
    private final String resetCode;

    ANSIFont(String code, String resetCode) {
        this.code = code;
        this.resetCode = resetCode;
    }

    @Override
    public String getRevertCode() {
        return resetCode;
    }

    public String getCode() {
        return code;
    }

    @Override
    public StyleType getType() {
        return StyleType.FONT;
    }


    public static ANSIFont fromString(String font) {
        font = font.toUpperCase().trim();
        if("BOLD".equals(font)) {
            return BOLD;
        } else if("ITALIC".equals(font)) {
            return ITALIC;
        } else if("UNDERLINE".equals(font)) {
            return UNDERLINE;
        } else if("REVERSE".equals(font)) {
            return REVERSE;
        } else if("STRIKE".equals(font)) {
            return STRIKE;
        }
        return null;
    }

}
