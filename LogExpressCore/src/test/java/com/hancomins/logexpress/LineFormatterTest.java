package com.hancomins.logexpress;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.*;

public class LineFormatterTest {


    // 레벨 제한자 테스트
    @Test
    public void levelQualifierTest() {
        LineFormatter lineFormatter = LineFormatter.parse("{time::hh  @ D e b u g,messages,\"error\",'info','c,k',c\\,j\\,k   }({message@ info, messages, 'c,k', 'c,j,k'  })({message@  warn  })({text:: END   @error})({text:::::::::: FATAL   @fatal})");
        LineCombiner lineCombiner = lineFormatter.getLineCombiner();
        // 현재 시간을 hh:mm:ss 로 표시
        String time = new SimpleDateFormat("HH").format(new Date());

        String value = lineCombiner.combine(new Line(lineFormatter, Level.TRACE, "messages", "M", null, 2)).toString();
        assertEquals (value, "()()()()\n");
        System.out.println(value);
        value = lineCombiner.combine(new Line(lineFormatter, Level.DEBUG, "messages", "M", null, 2)).toString();
        assertEquals(time + "()()()()\n", value);
        System.out.println(value);
        value = lineCombiner.combine(new Line(lineFormatter, Level.INFO, "messages", "M", null, 2)).toString();
        assertEquals(time + "(M)()()()\n", value);
        System.out.println(value);
        value = lineCombiner.combine(new Line(lineFormatter, Level.WARN, "messages", "M", null, 2)).toString();
        assertEquals(time + "(M)(M)()()\n", value);
        System.out.println(value);
        value = lineCombiner.combine(new Line(lineFormatter, Level.ERROR, "messages", "M", null, 2)).toString();
        System.out.println(value);
        assertEquals(time + "(M)(M)(END)()\n", value);
        value = lineCombiner.combine(new Line(lineFormatter, Level.FATAL, "messages", "M", null, 2)).toString();
        System.out.println(value);
        assertEquals(time + "(M)(M)(END)(FATAL)\n", value);
        value = lineCombiner.combine(new Line(lineFormatter, Level.FATAL, "messages", "M", null, 2)).toString();
        System.out.println(value);
        assertEquals(time + "(M)(M)(END)(FATAL)\n", value);

        value = lineCombiner.combine(new Line(lineFormatter, Level.FATAL, "info", "M", null, 2)).toString();
        System.out.println(value);
        assertEquals(time + "()(M)(END)(FATAL)\n", value);

        value = lineCombiner.combine(new Line(lineFormatter, Level.FATAL, "error", "M", null, 2)).toString();
        System.out.println(value);
        assertEquals(time + "()(M)(END)(FATAL)\n", value);

        value = lineCombiner.combine(new Line(lineFormatter, Level.FATAL, "c,k", "M", null, 2)).toString();
        System.out.println(value);
        assertEquals(time + "(M)(M)(END)(FATAL)\n", value);

        value = lineCombiner.combine(new Line(lineFormatter, Level.FATAL, "c,j,k", "M", null, 2)).toString();
        System.out.println(value);
        assertEquals(time + "(M)(M)(END)(FATAL)\n", value);

        value = lineCombiner.combine(new Line(lineFormatter, Level.FATAL, "none", "M", null, 2)).toString();
        System.out.println(value);
        assertEquals("()(M)(END)(FATAL)\n", value);

        /** 출력 결과 예
         ()()()()

         04()()()()

         04(M)()()()

         04(M)(M)()()

         04(M)(M)(END)()

         04(M)(M)(END)(FATAL)

         04(M)(M)(END)(FATAL)

         04()(M)(END)(FATAL)

         04()(M)(END)(FATAL)

         04(M)(M)(END)(FATAL)

         04(M)(M)(END)(FATAL)

         ()(M)(END)(FATAL)
         */


    }


    @Test
    public void lineFormatterTestForTargetLevel() {
        LineFormatter lineFormatter = LineFormatter.parse("{time::HH:mm@error}/{message[10:12 ]}/ {class[:-2]}.{method[ 50:50]}()#{line[10:]@error}");
        LineCombiner lineCombiner = lineFormatter.getLineCombiner();
        String value = lineCombiner.combine(new Line(lineFormatter, Level.ERROR, "marker", "012345", null, 2)).toString();
        System.out.println(value.replace(' ', '_'));

        String time = new SimpleDateFormat("HH:mm").format(new Date());

        assertEquals(time + "/012345____/_st.___________________lineFormatterTestForTargetLevel()#", value.replace(' ', '_').trim().replaceAll("#.*$", "#"));

        // 출력 결과 예: 04:57/012345____/_st.___________________lineFormatterTestForTargetLevel()#69

    }


    @Test
    public void lineFormatterTest() {
        LineFormatter lineFormatter = LineFormatter.parse("/{message[ 10:10 ]@TRACE}/ {class[:-2]}.{method[ 30:]}()#{line[10:]}");
        LineCombiner lineCombiner = lineFormatter.getLineCombiner();
        String value = lineCombiner.combine(new Line(lineFormatter, Level.INFO, "marker", "012345", null, 2)).toString();
        System.out.println(value.replace(' ', '_'));

        //출력 결과 예: /__012345__/_st._____________lineFormatterTest()#115

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

    @Test
    public void complexLevelAndMarkerTest() {
        LineFormatter lineFormatter = LineFormatter.parse("{time::HH:mm:ss@'info',debug}/{marker@warn}/{message}");
        LineCombiner lineCombiner = lineFormatter.getLineCombiner();

        // INFO 레벨, "error" 마커
        String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
        String value = lineCombiner.combine(new Line(lineFormatter, Level.INFO, "error", "Test message", null, 2)).toString();
        System.out.println(value);
        assertEquals("//Test message\n", value);

        // DEBUG 레벨, "info" 마커
        value = lineCombiner.combine(new Line(lineFormatter, Level.DEBUG, "info", "Another message", null, 2)).toString();
        System.out.println(value);
        assertEquals(time + "//Another message\n", value);

        // WARN 레벨, "debug" 마커
        value = lineCombiner.combine(new Line(lineFormatter, Level.WARN, "debug", "Message with warning level", null, 2)).toString();
        System.out.println(value);
        assertEquals("/debug/Message with warning level\n", value);
    }

    @Test
    public void testMessageFormattingWithLength() {
        LineFormatter lineFormatter = LineFormatter.parse(".{message[8:8]@info}.");
        LineCombiner lineCombiner = lineFormatter.getLineCombiner();

        // INFO 레벨, 메시지가 출력됨
        String value = lineCombiner.combine(new Line(lineFormatter, Level.INFO, "marker", "Short", null, 2)).toString();
        System.out.println(value);
        assertEquals(".Short   .", value.trim()); // 짧은 메시지는 오른쪽 정렬됨


        // INFO 레벨, 긴 메시지 잘림
        value = lineCombiner.combine(new Line(lineFormatter, Level.INFO, "marker", "This message is too long", null, 2)).toString();
        System.out.println(value);
        assertEquals(".This mes.", value.trim()); // 긴 메시지는 잘려나감



    }


    @Test
    public void testMessageCutAlign() {
        LineFormatter lineFormatter = LineFormatter.parse("[{level}] .{message[ 5:5 ]}.");
        LineCombiner lineCombiner = lineFormatter.getLineCombiner();
        String value = lineCombiner.combine(new Line(lineFormatter, Level.INFO, "", "1234567890", null, 2)).toString();
        System.out.println(value);
        assertEquals("[INFO] .34567.\n", value);

        lineFormatter = LineFormatter.parse("[{level}] .{message[ 5:5]}.");
        lineCombiner = lineFormatter.getLineCombiner();
        value = lineCombiner.combine(new Line(lineFormatter, Level.INFO, "", "1234567890", null, 2)).toString();
        System.out.println(value);
        assertEquals("[INFO] .67890.\n", value);

        lineFormatter = LineFormatter.parse("[{level}] .{message[ 15:]}.");
        lineCombiner = lineFormatter.getLineCombiner();
        value = lineCombiner.combine(new Line(lineFormatter, Level.INFO, "", "1234567890", null, 2)).toString();
        System.out.println(value);
        assertEquals("[INFO] .     1234567890.\n", value);

        lineFormatter = LineFormatter.parse("[{level}] .{message[ 15: ]}.");
        lineCombiner = lineFormatter.getLineCombiner();
        value = lineCombiner.combine(new Line(lineFormatter, Level.INFO, "", "1234567890", null, 2)).toString();
        System.out.println(value);
        assertEquals("[INFO] .  1234567890   .\n", value);



    }



    @Test
    public void testDifferentLengthAlignments() {
        LineFormatter lineFormatter = LineFormatter.parse(".{message[ 15:15 ]@center}.");
        LineCombiner lineCombiner = lineFormatter.getLineCombiner();

        String value = lineCombiner.combine(new Line(lineFormatter, Level.INFO, "center", "Center", null, 2)).toString();
        System.out.println(value);
        assertEquals(".    Center     .\n", value); // 중앙 정렬된 메시지

        lineFormatter = LineFormatter.parse(".{message[ 15:15]@right}.");
        lineCombiner = lineFormatter.getLineCombiner();

        value = lineCombiner.combine(new Line(lineFormatter, Level.INFO, "right", "Right", null, 2)).toString();
        System.out.println(value);
        assertEquals(".          Right.\n", value); // 오른쪽 정렬된 메시지
    }



}