package com.hancomins.logexpress.configuration;

import com.hancomins.logexpress.Level;
import com.hancomins.logexpress.LinePatternItemType;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;

@SuppressWarnings("ALL")
public class ConfigurationParserTest extends TestCase {

    /**
     * https://github.com/hancomins/LogExpress/issues/5
     */
    @Test
    public void testAllLevelStyleParser() throws IOException {
        String properties = "[configuration]\nstyle.all.level=green\nstyle.trace.level=+red";
        StringReader reader = new StringReader(properties);
        Configuration configuration = ConfigurationParser.parse(reader);
        StyleOption styleOption = configuration.defaultStyleOption();

        String ansiCode = styleOption.getAnsiCode(Level.TRACE, LinePatternItemType.Level);
        assertEquals("GREEN;RED", StyleOption.codeToStyleNames(ansiCode));

    }

    @Test
    public void testParse() throws IOException {
        Configuration configuration = new Configuration();

        configuration.setDefaultLevel(Level.WARN);
        configuration.setDefaultMarker("test0");
        configuration.setQueueSize(213123);



        StyleOption defaultStyleOption = configuration.defaultStyleOption();
        defaultStyleOption.enableConsole(true)
                .setStyle(Level.INFO, LinePatternItemType.Message, ANSIColor.GREEN, null)
                .setStyle(Level.WARN, LinePatternItemType.Message, ANSIColor.GREEN, null)
                .setStyle(Level.ERROR, LinePatternItemType.Message, ANSIColor.RED, ANSIColor.BLACK)
                .setStyle(Level.DEBUG, LinePatternItemType.Message, ANSIColor.CYAN, null);

        defaultStyleOption.setStyle(Level.TRACE, LinePatternItemType.Message, ANSIColor.WHITE, ANSIColor.BLUE);

        // WriterOption 5개 이상 설정
        for (int i = 0; i < 5; i++) {
            WriterOption writerOption = configuration.newWriterOption("test" + i);
            writerOption.setLevel(i % 2 == 0 ? Level.INFO : Level.ERROR);
            writerOption.setFile("./log" + i + ".txt");
            writerOption.setLinePattern(i % 2 == 0 ? "{time} {level} {message}" : "{level} {message} {time}");
            writerOption.setBufferSize(1024 + i * 512);
            writerOption.setMaxSize(512 + i * 256);
            writerOption.setHistory(30 + i * 10);
            writerOption.addWriterType(i % 2 == 0 ? WriterType.Console : WriterType.File);
            writerOption.addWriterType(WriterType.File);
        }


        String value = configuration.toString();
        System.out.println(value);

        Configuration configurationParsed = Configuration.newConfiguration(new StringReader(value));
        assertEquals(configuration.toString(), configurationParsed.clone().toString());
    }



}