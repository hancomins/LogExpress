package com.hancomins.logexpress.util;

public enum ANSIColor {
    BLACK("30", "40"),
    RED("31", "41"),
    GREEN("32", "42"),
    YELLOW("33", "43"),
    BLUE("34", "44"),
    PURPLE("35", "45"),
    CYAN("36", "46"),
    WHITE("37", "47");




    private final String code;
    private final String backgroundCode;
    private final String ansiCode;
    private final String ansiBackgroundCode;


    public static final String ANSI_RESET = "\u001B[0m";




    public static String codeToColorNames(String color) {
        StringBuilder sb = new StringBuilder();
        color = color.replace("\u001B[", "").replace("\u001b[", "");
        String[] colors = color.split(";");

        for(String c : colors) {
            if(c == null || c.isEmpty()) {
                continue;
            }
            if(c.endsWith("m") || c.endsWith("M")) {
                c = c.substring(0, c.length() - 1);
            }
            for(ANSIColor colorCode : ANSIColor.values()) {
                if(colorCode.code.equals(c) || colorCode.backgroundCode.equals(c)) {
                    sb.append(colorCode.name()).append(";");
                    break;
                }
            }
        }
        if(sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }

        return sb.toString();
    }


    public static ANSIColor fromString(String color) {
        color = color.toUpperCase().trim();
        if("BLACK".equals(color)) {
            return BLACK;
        } else if("RED".equals(color)) {
            return RED;
        } else if("GREEN".equals(color)) {
            return GREEN;
        } else if("YELLOW".equals(color)) {
            return YELLOW;
        } else if("BLUE".equals(color)) {
            return BLUE;
        } else if("PURPLE".equals(color)) {
            return PURPLE;
        } else if("CYAN".equals(color)) {
            return CYAN;
        } else if("WHITE".equals(color)) {
            return WHITE;
        }
        return null;
    }


    ANSIColor(String code, String backgroundCode) {
        this.code = code;
        this.backgroundCode = backgroundCode;
        this.ansiCode = "\u001B[" + code + "m";
        this.ansiBackgroundCode = "\u001B[" + backgroundCode + "m";
    }

    public String getANSICode() {
        return ansiCode;
    }

    @SuppressWarnings("unused")
    public String getBackgroundANSICode() {
        return ansiBackgroundCode;
    }



    public static String combineCode(ANSIColor color, ANSIColor background) {
        if(background == null) {
            return "\u001B[" + color.code;
        }

        return "\u001B[" + color.code + ";" + background.backgroundCode + "m";
    }

}