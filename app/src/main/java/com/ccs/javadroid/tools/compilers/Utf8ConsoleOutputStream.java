package com.ccs.javadroid.tools.compilers;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/** Decodes console bytes without splitting UTF-8 characters between writes or flushes. */
final class Utf8ConsoleOutputStream extends OutputStream {
    private final OutputStream capture;
    private final Consumer<String> output;
    private final ByteBuffer pending = ByteBuffer.allocate(4096);
    private final CharBuffer text = CharBuffer.allocate(4096);
    private final CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder()
            .onMalformedInput(CodingErrorAction.REPLACE)
            .onUnmappableCharacter(CodingErrorAction.REPLACE);
    private boolean closed;

    Utf8ConsoleOutputStream(OutputStream capture, Consumer<String> output) {
        this.capture = capture;
        this.output = output;
    }

    @Override
    public synchronized void write(int value) throws IOException {
        ensureOpen();
        capture.write(value);
        append((byte) value);
    }

    @Override
    public synchronized void write(byte[] bytes, int offset, int length) throws IOException {
        ensureOpen();
        if (offset < 0 || length < 0 || length > bytes.length - offset) {
            throw new IndexOutOfBoundsException();
        }
        capture.write(bytes, offset, length);
        for (int i = offset; i < offset + length; i++) append(bytes[i]);
    }

    private void append(byte value) throws IOException {
        if (!pending.hasRemaining()) decode(false);
        pending.put(value);
        if (value == '\n') decode(false);
    }

    private void decode(boolean end) throws IOException {
        pending.flip();
        java.nio.charset.CoderResult result;
        do {
            result = decoder.decode(pending, text, end);
            emit();
            if (result.isError()) result.throwException();
        } while (result.isOverflow());
        pending.compact();
        if (end) {
            do {
                result = decoder.flush(text);
                emit();
            } while (result.isOverflow());
        }
    }

    private void emit() {
        text.flip();
        if (text.hasRemaining()) output.accept(text.toString());
        text.clear();
    }

    @Override
    public synchronized void flush() throws IOException {
        if (closed) return;
        capture.flush();
        decode(false);
    }

    @Override
    public synchronized void close() throws IOException {
        if (closed) return;
        decode(true);
        capture.flush();
        closed = true;
    }

    private void ensureOpen() throws IOException {
        if (closed) throw new IOException("Console output is closed");
    }
}
