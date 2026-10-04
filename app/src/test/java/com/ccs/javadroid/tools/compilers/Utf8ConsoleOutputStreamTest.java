package com.ccs.javadroid.tools.compilers;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Utf8ConsoleOutputStreamTest {
    @Test
    public void cyrillicAndEmojiSurviveEveryPossibleChunkBoundary() throws Exception {
        String expected = "Факторіал 5 = 120\nПривіт, світе! 😀\n";
        byte[] bytes = expected.getBytes(StandardCharsets.UTF_8);
        for (int split = 0; split <= bytes.length; split++) {
            ByteArrayOutputStream capture = new ByteArrayOutputStream();
            StringBuilder shown = new StringBuilder();
            Utf8ConsoleOutputStream stream = new Utf8ConsoleOutputStream(capture, shown::append);
            stream.write(bytes, 0, split);
            stream.flush();
            stream.write(bytes, split, bytes.length - split);
            stream.close();
            assertEquals("split at " + split, expected, shown.toString());
            assertEquals(expected, capture.toString("UTF-8"));
        }
    }

    @Test
    public void singleByteWritesAndFlushesPreserveIncompleteCharacters() throws Exception {
        String expected = "Україна 🇺🇦";
        StringBuilder shown = new StringBuilder();
        Utf8ConsoleOutputStream stream = new Utf8ConsoleOutputStream(
                new ByteArrayOutputStream(), shown::append);
        for (byte value : expected.getBytes(StandardCharsets.UTF_8)) {
            stream.write(value & 0xff);
            stream.flush();
        }
        stream.close();
        assertEquals(expected, shown.toString());
    }

    @Test
    public void promptAppearsBeforeReadingInputAndIsNotRepeatedOnClose() throws Exception {
        StringBuilder shown = new StringBuilder();
        PrintStream stream = new PrintStream(new Utf8ConsoleOutputStream(
                new ByteArrayOutputStream(), shown::append), true, "UTF-8");
        stream.print("Введіть ім’я: ");
        stream.flush();
        assertEquals("Введіть ім’я: ", shown.toString());
        stream.println("Дмитро");
        assertTrue(shown.toString().endsWith("Дмитро\n"));
        String finished = shown.toString();
        stream.close();
        assertEquals(finished, shown.toString());
    }

    @Test
    public void largeOutputCrossesDecoderBufferBoundary() throws Exception {
        String expected = "я😀".repeat(3000) + "\n";
        StringBuilder shown = new StringBuilder();
        Utf8ConsoleOutputStream stream = new Utf8ConsoleOutputStream(
                new ByteArrayOutputStream(), shown::append);
        stream.write(expected.getBytes(StandardCharsets.UTF_8));
        stream.close();
        assertEquals(expected, shown.toString());
    }
}
