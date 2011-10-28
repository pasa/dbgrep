package org.parilin.dbgrep;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;

import javax.annotation.concurrent.NotThreadSafe;

import org.parilin.dbgrep.util.InputSupplier;

/**
 * Reads and decode byte channel to Unicode.
 */
@NotThreadSafe
public class ChannelReader implements Reader {

    private final InputSupplier<? extends ReadableByteChannel> in;

    private final CharsetDecoder decoder;

    private ReadableByteChannel channel;

    private ByteBuffer buffer;

    boolean endOfInput = false;

    public ChannelReader(InputSupplier<? extends ReadableByteChannel> in, Charset charset, ByteBuffer buffer) {
        this(in, requireNonNull(charset).newDecoder(), buffer);
    }

    public ChannelReader(InputSupplier<? extends ReadableByteChannel> in, CharsetDecoder decoder, ByteBuffer buffer) {
        this.in = requireNonNull(in);
        this.decoder = requireNonNull(decoder);
        this.buffer = requireNonNull(buffer);
    }

    @Override
    public boolean read(CharBuffer out) throws IOException {
        if (channel == null) {
            channel = in.getInput();
            buffer.clear();
        }
        int readBytes = channel.read(buffer);
        if (readBytes > 0 && buffer.hasRemaining()) {
            readBytes = channel.read(buffer);
        }
        boolean endOfInput = false; // buffer.hasRemaining(); // buffer is not full (end of input)
        if (readBytes == -1) {
            endOfInput = true;
        }
        buffer.flip();
        CoderResult cr;
        if (buffer.hasRemaining() || endOfInput) {
            cr = decoder.decode(buffer, out, endOfInput);
        } else {
            cr = CoderResult.UNDERFLOW;
        }
        if (endOfInput && cr.isUnderflow()) {
            cr = decoder.flush(out);
            if (cr.isUnderflow()) {
                buffer.compact(); // copy buffer remain to the buffer start
                return false;
            } else {
                System.err.println(buffer.hasRemaining());
            }
        }
        buffer.compact(); // copy buffer remain to the buffer start
        return true;
    }

    @Override
    public void close() throws IOException {
        if (channel != null) {
            channel.close();
            channel = null;
        }
    }
}
