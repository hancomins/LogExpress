package com.hancomins.LogExpress;

import org.junit.Test;

import static org.junit.Assert.*;

public class LineFormatterTest {


    @Test
    public void lineFormatterTest() {
        LineFormatter lineFormatter = LineFormatter.parse("/{message[ 10:10 ]}/ {class[:-2]}.{method[ 30:]}()#{line[10:]}");
        LineCombiner lineCombiner = lineFormatter.getLineCombiner();
        String value = lineCombiner.combine(new Line(lineFormatter, Level.INFO, "marker", "012345", null, 2)).toString();
        System.out.println(value.replace(' ', '_'));
        // 마지막에 라인 번호는 코드를 수정하면 변할 수 있으므로 테스트 이후 나온 값으로 변경해야 합니다.
        assertEquals("/__012345__/_st._____________lineFormatterTest()#14", value.replace(' ', '_').trim());

    }

    @Test
    public void parseLenRangeTest() {
        LineFormatter.LenRange lenRange = LineFormatter.parseLenRange("[ 1:10]");
        assertEquals(1, lenRange.min);
        assertEquals(10, lenRange.max);
        assertTrue(lenRange.align == LineFormatter.LenRange.ALIGN_RIGHT);

        lenRange = LineFormatter.parseLenRange("[ 1:10 ]");
        assertEquals(1, lenRange.min);
        assertEquals(10, lenRange.max);
        assertTrue(lenRange.align == LineFormatter.LenRange.ALIGN_CENTER);

        lenRange = LineFormatter.parseLenRange("[ :10]");
        assertEquals(0, lenRange.min);
        assertEquals(10, lenRange.max);
        assertTrue(lenRange.align == LineFormatter.LenRange.ALIGN_RIGHT);

        lenRange = LineFormatter.parseLenRange("[ :]");
        assertEquals(0, lenRange.min);
        assertEquals(0, lenRange.max);
        assertTrue(lenRange.align == LineFormatter.LenRange.ALIGN_RIGHT);

        lenRange = LineFormatter.parseLenRange("[100:]");
        assertEquals(100, lenRange.min);
        assertEquals(0, lenRange.max);
        assertFalse(lenRange.align == LineFormatter.LenRange.ALIGN_RIGHT);
    }

}