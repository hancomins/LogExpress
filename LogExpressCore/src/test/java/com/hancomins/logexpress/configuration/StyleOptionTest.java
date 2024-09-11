package com.hancomins.logexpress.configuration;

import com.hancomins.logexpress.Level;
import com.hancomins.logexpress.LinePatternItemType;
import junit.framework.TestCase;
import org.junit.Test;

public class StyleOptionTest extends TestCase {

    @Test
    public void testStyleOption() {
        StyleOption styleOption = new StyleOption();
        styleOption.setStyle("info", "message", "+green");
        styleOption.setStyle("info", "message", "red");
        styleOption.setStyle("info", "message", "+");

        String code = styleOption.getAnsiCode(Level.INFO, LinePatternItemType.Message);
        String styleNames = StyleOption.codeToStyleNames(code);
        assertEquals("RED", styleNames);


        styleOption.setStyle("info", "message", "+green");
        styleOption.setStyle("all", "message", "+bold");

        code = styleOption.getAnsiCode(Level.INFO, LinePatternItemType.Message);
        styleNames = StyleOption.codeToStyleNames(code);
        assertEquals("RED;GREEN;BOLD", styleNames);


        styleOption.setStyle("all", "message", "");
        code = styleOption.getAnsiCode(Level.INFO, LinePatternItemType.Message);
        styleNames = StyleOption.codeToStyleNames(code);
        assertEquals("", styleNames);

    }

}