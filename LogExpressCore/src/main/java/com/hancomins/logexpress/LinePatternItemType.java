package com.hancomins.logexpress;

public enum LinePatternItemType {
    Level,
    Thread,
    Tid,
    Method,
    File,
    Class,
    ClassPackage,
    ClassName,
    Line,
    Time,
    Text,
    Message,
    Hostname,
    Marker,
    Pid,
    Caller,
    CallerPackage,
    CallerSimple;

    LinePatternItemType() {

    }




    public static LinePatternItemType typeNameOf(String type) {
        if(type == null) {
            return null;
        }
        LinePatternItemType[] types = values();
        //noinspection ForLoopReplaceableByForEach
        for(int i = 0; i < types.length; ++i) {
            if(types[i].name().equalsIgnoreCase(type)) {
                return types[i];
            }
        }
        if("class-package".equalsIgnoreCase(type)) {
            return ClassPackage;
        }
        if("caller-package".equalsIgnoreCase(type)) {
            return CallerPackage;
        }
        if("class-name".equalsIgnoreCase(type) || "class-simple".equalsIgnoreCase(type)) {
            return ClassName;
        }
        if("caller-simple".equalsIgnoreCase(type) || "caller-name".equalsIgnoreCase(type)) {
            return CallerSimple;
        }

        return null;
    }

    static String[] getTypeNameArray() {
        return new String[] {"marker", "level", "thread","tid", "method", "class", "class-name","class-package","file", "line", "text", "message", "time", "hostname", "pid","caller","caller-simple", "caller-package"};
    }
}