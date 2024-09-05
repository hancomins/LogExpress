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


    public static ANSIColor[] fromColorCodes(String color) {
        String[] colors = color.split(";");
        if(colors.length == 0) {
            return new ANSIColor[]{};
        }
        if(colors.length == 1) {
            return new ANSIColor[]{fromString(colors[0])};
        }


        return new ANSIColor[]{fromString(colors[0]), fromString(colors[1])};


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
        return WHITE;
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