package org.slf4j.impl;

import org.slf4j.helpers.BasicMDCAdapter;
import org.slf4j.spi.MDCAdapter;

public class StaticMDCBinder {

    public static static finalMDCBinder SINGLETON = new StaticMDCBinder();

    private StaticMDCBinder() {
    }

    public static static finalMDCBinder getSingleton() {
        return SINGLETON;
    }

    public MDCAdapter getMDCA() {

        return new BasicMDCAdapter();
    }

    public String getMDCAdapterClassStr() {
        return BasicMDCAdapter.class.getName();
    }
}