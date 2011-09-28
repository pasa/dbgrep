package org.parilin.dbgrep;

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
 * Reads and decode channel to Unicode.
 */
@NotThreadSafe
public class ChannelReader implements Reader {

    public static final int DEFAULT_BUFFER_SIZE = 8192;

    private final InputSupplier<? extends ReadableByteChannel> in;

    private final CharsetDecoder decoder;

    private ReadableByteChannel channel;

    private ByteBuffer buffer;

    private final int bufferSize;

    public ChannelReader(InputSupplier<? extends ReadableByteChannel> in, Charset charset) {
        this(in, charset.newDecoder());
    }

    public ChannelReader(InputSupplier<? extends ReadableByteChannel> in, CharsetDecoder decoder) {
        this(in, decoder, DEFAULT_BUFFER_SIZE);
    }

    public ChannelReader(InputSupplier<? extends ReadableByteChannel> in, Charset charset, int bufferSize) {
        this(in, charset.newDecoder(), bufferSize);
    }

    public ChannelReader(InputSupplier<? extends ReadableByteChannel> in, CharsetDecoder decoder, int bufferSize) {
        this.in = in;
        this.decoder = decoder;
        this.bufferSize = bufferSize;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    @Override
    public boolean read(CharBuffer out) throws IOException {
        if (channel == null) {
            channel = in.getInput();
            buffer = ByteBuffer.allocateDirect(bufferSize);
        }
        int readBytes = channel.read(buffer);
        boolean endOfInput = readBytes == -1;
        out.clear();
        decoder.decode(buffer, out, endOfInput);
        if (endOfInput) { // flush if channel finished
            decoder.flush(out);
        }
        buffer.compact(); // copy buffer remain to the buffer start
        return !endOfInput;
    }

    @Override
    public void close() throws Exception {
        if (channel != null) {
            channel.close();
            channel = null;
        }
    }

    public static void main(String[] args) throws Exception {
        String test = "abc";
        byte[] bytes = test.getBytes("UTF-16");
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        CharsetDecoder dec = Charset.forName("UTF-16").newDecoder();
        CharBuffer cb = CharBuffer.allocate(test.length());
        System.out.println(bb.capacity());
        bb.limit(5);
        CoderResult result = dec.decode(bb, cb, false);
        int lim = cb.limit();
        cb.flip();
        System.out.print("AAA");
        System.out.print(cb);
        System.out.println("AAA");
        cb.position(cb.limit());
        cb.limit(lim);
        System.out.println(bb);
        System.out.println(result);
        int rem = bb.capacity() - bb.limit();
        bb.compact();
        bb.limit(rem);
        System.out.println(bb);
        dec.decode(bb, cb, true);
        cb.clear();
        System.out.print("AAA");
        System.out.print(cb);
        System.out.println("AAA");
    }
}
