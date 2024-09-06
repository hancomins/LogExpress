package com.hancomins.logexpress.configuration;


public interface ANSIStyle {

    enum StyleType {
        COLOR,
        FONT
    }
    String name();

    String getRevertCode();
    String getCode();
    StyleType getType();



}