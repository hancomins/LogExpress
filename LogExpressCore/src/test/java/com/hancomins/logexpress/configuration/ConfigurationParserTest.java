package com.hancomins.logexpress.configuration;

import com.hancomins.logexpress.Level;
import com.hancomins.logexpress.LinePatternItemType;
import com.hancomins.logexpress.util.ANSIColor;
import junit.framework.TestCase;
import org.junit.Test;

public class ConfigurationParserTest extends TestCase {

    @Test
    public void testParse() {
        Configuration configuration = new Configuration();


        ColorOption defaultColorOption = configuration.defaultColorOption();
        defaultColorOption.enableConsole(true)
                .putColorCode(Level.INFO, LinePatternItemType.Message, ANSIColor.GREEN, null)
                .putColorCode(Level.WARN, LinePatternItemType.Message, ANSIColor.GREEN, null)
                .putColorCode(Level.ERROR, LinePatternItemType.Message, ANSIColor.RED, ANSIColor.BLACK)
                .putColorCode(Level.DEBUG, LinePatternItemType.Message, ANSIColor.CYAN, null)
                .putColorCode(Level.TRACE, LinePatternItemType.Message, ANSIColor.WHITE, ANSIColor.BLUE);

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
            writerOption.setColorOption(defaultColorOption.clone());
        }


        String value = configuration.toString();
        System.out.println(value);




    }

}