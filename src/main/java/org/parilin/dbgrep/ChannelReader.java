package org.parilin.dbgrep;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import javax.annotation.concurrent.NotThreadSafe;

import org.parilin.dbgrep.util.InputSupplier;

/**
 * Reads and decode channel to Unicode.
 */
@NotThreadSafe
public class ChannelReader implements Reader {

    private final InputSupplier<? extends ReadableByteChannel> in;

    private final CharsetDecoder decoder;

    private ReadableByteChannel channel;

    private ByteBuffer buffer;

    public ChannelReader(InputSupplier<? extends ReadableByteChannel> in, Charset charset, ByteBuffer buffer) {
        this(in, charset.newDecoder(), buffer);
    }

    public ChannelReader(InputSupplier<? extends ReadableByteChannel> in, CharsetDecoder decoder, ByteBuffer buffer) {
        this.in = in;
        this.decoder = decoder;
        this.buffer = buffer;
    }

    @Override
    public boolean read(CharBuffer out) throws IOException {
        if (channel == null) {
            channel = in.getInput();
            buffer.clear();
        }
        int readBytes = channel.read(buffer);
        boolean endOfInput = readBytes == -1;
        buffer.flip();
        decoder.decode(buffer, out, endOfInput);
        if (endOfInput) { // flush if channel finished
            decoder.flush(out);
        }
        buffer.compact(); // copy buffer remain to the buffer start
        return !endOfInput;
    }

    @Override
    public void close() throws IOException {
        if (channel != null) {
            channel.close();
            channel = null;
        }
    }
}
