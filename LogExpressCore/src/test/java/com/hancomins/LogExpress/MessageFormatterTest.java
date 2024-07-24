package com.hancomins.LogExpress;

import org.junit.Test;

import static org.junit.Assert.*;

public class MessageFormatterTest {

    @Test
    public void testToString() {
        MessageFormatter formatter = MessageFormatter.newFormat("Hello, {}!", "World");
        assertEquals("Hello, World!", formatter.toString());

        formatter = MessageFormatter.newFormat("Hello, {}!", "World","1","2","3");
        assertEquals("Hello, World!", formatter.toString());

        formatter = MessageFormatter.newFormat("Hello, {}{}{}{}{}{}{}!", "World","1","2","3");
        assertEquals("Hello, World123{}{}{}!", formatter.toString());

        formatter = MessageFormatter.newFormat("Hello, {}{}{}{}{}{}{-1}!", "World","1","2","3");
        assertEquals("Hello, World123{}{}{-1}!", formatter.toString());



        formatter = MessageFormatter.newFormat("Hello, {}{}{}{}{}!", "World","1","2","3");
        assertEquals("Hello, World123{}!", formatter.toString());

        formatter = MessageFormatter.newFormat("Hello, {0} {0} {0} {0}!", "World","OK");
        assertEquals("Hello, World World World World!", formatter.toString());

        formatter = MessageFormatter.newFormat("Hello, {2} {} {0} {1}!", "World","OK","Hi");
        assertEquals("Hello, Hi World World OK!", formatter.toString());

        formatter = MessageFormatter.newFormat("Hello, {2} {} {0} {1} {3}!", "World","OK","Hi");
        assertEquals("Hello, Hi World World OK {3}!", formatter.toString());

    }
}