package com.hancomins.logexpress.util;


import org.junit.Test;

import static org.junit.Assert.*;

public class StringUtilTest {

    @Test
    public void testSplitStringWithSeparator_basic() {
        String input = "value1,value2,value3";
        char separator = ',';
        String[] expected = {"value1", "value2", "value3"};

        String[] result = StringUtil.splitStringWithSeparator(input, separator, false);

        assertArrayEquals(expected, result);
    }

    @Test
    public void testSplitStringWithSeparator_withQuotes() {
        String input = "value1,\"value2,value3\",value4";
        char separator = ',';
        String[] expectedWithoutQuotes = {"value1", "value2,value3", "value4"};
        String[] expectedWithQuotes = {"value1", "\"value2,value3\"", "value4"};

        // 테스트: 따옴표를 포함하지 않음
        String[] resultWithoutQuotes = StringUtil.splitStringWithSeparator(input, separator, false);
        assertArrayEquals(expectedWithoutQuotes, resultWithoutQuotes);

        // 테스트: 따옴표를 포함함
        String[] resultWithQuotes = StringUtil.splitStringWithSeparator(input, separator, true);
        assertArrayEquals(expectedWithQuotes, resultWithQuotes);
    }

    @Test
    public void testSplitStringWithSeparator_withSingleQuotes() {
        String input = "value1,'value2,value3',value4";
        char separator = ',';
        String[] expectedWithoutQuotes = {"value1", "value2,value3", "value4"};
        String[] expectedWithQuotes = {"value1", "'value2,value3'", "value4"};

        // 테스트: 따옴표를 포함하지 않음
        String[] resultWithoutQuotes = StringUtil.splitStringWithSeparator(input, separator, false);
        assertArrayEquals(expectedWithoutQuotes, resultWithoutQuotes);

        // 테스트: 따옴표를 포함함
        String[] resultWithQuotes = StringUtil.splitStringWithSeparator(input, separator, true);
        assertArrayEquals(expectedWithQuotes, resultWithQuotes);
    }

    @Test
    public void testSplitStringWithSeparator_withEscapedSeparator() {
        String input = "value1,value\\,2,value3";
        char separator = ',';
        String[] expected = {"value1", "value,2", "value3"};

        String[] result = StringUtil.splitStringWithSeparator(input, separator, false);

        assertArrayEquals(expected, result);
    }

    @Test
    public void testSplitStringWithSeparator_emptyInput() {
        String input = "";
        char separator = ',';
        String[] expected = {};

        String[] result = StringUtil.splitStringWithSeparator(input, separator, false);

        assertArrayEquals(expected, result);
    }

    @Test
    public void testSplitStringWithSeparator_onlySeparators() {
        String input = ",,,";
        char separator = ',';
        String[] expected = {"", "", "", ""};

        String[] result = StringUtil.splitStringWithSeparator(input, separator, false);

        assertArrayEquals(expected, result);
    }
}
