package org.parilin.dbgrep;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import javax.annotation.concurrent.NotThreadSafe;

import org.parilin.dbgrep.util.InputSupplier;

@NotThreadSafe
public class FileChannelReader implements Reader {

    public static final int DEFAULT_BUFFER_SIZE = 8192;

    private final InputSupplier<? extends ReadableByteChannel> in;

    private final CharsetDecoder decoder;

    private ReadableByteChannel channel;

    private ByteBuffer buffer;

    private final int bufferSize;

    public FileChannelReader(InputSupplier<? extends ReadableByteChannel> in, Charset charset) {
        this(in, charset.newDecoder());
    }

    public FileChannelReader(InputSupplier<? extends ReadableByteChannel> in, CharsetDecoder decoder) {
        this(in, decoder, DEFAULT_BUFFER_SIZE);
    }

    public FileChannelReader(InputSupplier<? extends ReadableByteChannel> in, Charset charset, int bufferSize) {
        this(in, charset.newDecoder(), bufferSize);
    }

    public FileChannelReader(InputSupplier<? extends ReadableByteChannel> in, CharsetDecoder decoder, int bufferSize) {
        this.in = in;
        this.decoder = decoder;
        this.bufferSize = bufferSize;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    @Override
    public boolean read(CharBuffer buff) throws IOException {
        if (channel == null) {
            channel = in.getInput();
            buffer = ByteBuffer.allocateDirect(bufferSize);
        }
        buff.clear();
        channel.read(buffer);
        return false;
    }

    @Override
    public void close() throws Exception {
        if (channel != null) {
            channel.close();
            channel = null;
        }
    }
}
