package com.hancomins.logexpress.configuration;

import com.hancomins.logexpress.InLogger;

import java.util.Properties;

class StaticVariableReplacer {

    protected static String replace(String str) {
        return replaceProperty(replaceEnv(str), System.getProperties());
    }

    // This method takes a string as an argument. Replace all %key% in the string with an environment variable. %key% is the key of the environment variable. If the environment variable does not exist, %key% is returned as it is. If an environment variable exists, the value of the environment variable is returned.
    public static String replaceEnv(String str) {
        if(str == null) return null;
        int idx = str.indexOf("%");
        if(idx < 0) return str;
        int idx2 = str.indexOf("%", idx + 1);
        if(idx2 < 0) return str;
        String key = str.substring(idx + 1, idx2);
        String value = System.getenv(key);
        if(value == null) {
            value = "%" + key + "%";
            InLogger.ERROR("Unable to find the Env '" + key + "' defined in the configuration. (Config line: '" + str + "')");
        }
        return str.substring(0, idx) + value + replaceEnv(str.substring(idx2 + 1));
    }

    protected static String replaceProperty(String str) {
        return replaceProperty(str, System.getProperties());
    }

    protected static String replaceProperty(String str, Properties properties) {
        if(str == null) return null;
        int start = str.indexOf("${");
        if(start < 0) return str;
        int end = str.indexOf("}", start + 1);
        if(end < 0) return str;
        String key = str.substring(start + 2, end);
        String value = properties.getProperty(key);
        if(value == null) {
            InLogger.ERROR("Unable to find the property '" + key + "' defined in the configuration. (Config line: '" + str + "')");
            value = "${" + key + "}";
        }
        return str.substring(0, start) + value + replaceProperty(str.substring(end + 1), properties);
    }
}
