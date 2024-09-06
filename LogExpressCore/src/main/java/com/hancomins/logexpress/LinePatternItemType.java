package com.hancomins.logexpress;

public enum LinePatternItemType {
    Level,
    Thread,
    Tid,
    Method,
    File,

    Class,
    ClassName,
    Line,
    Time,
    Text,
    Message,
    Hostname,
    Marker,
    Pid,
    Caller,
    CallerSimple;


    private LinePatternItemType() {

    }




    public static LinePatternItemType typeNameOf(String type) {
        LinePatternItemType[] types = values();
        //noinspection ForLoopReplaceableByForEach
        for(int i = 0; i < types.length; ++i) {
            if(types[i].name().equalsIgnoreCase(type)) {
                return types[i];
            }
        }
        if(type.equalsIgnoreCase("class-name")) {
            return ClassName;
        }
        if(type.equalsIgnoreCase("caller-simple")) {
            return CallerSimple;
        }

        return null;
    }

    static String[] getTypeNameArray() {
        return new String[] {"marker", "level", "thread","tid", "method", "class", "class-name","file", "line", "text", "message", "time", "hostname", "pid","caller","caller-simple"};
    }
}