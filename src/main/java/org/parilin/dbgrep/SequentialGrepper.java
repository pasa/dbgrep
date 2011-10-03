package org.parilin.dbgrep;

import static java.util.Objects.requireNonNull;
import static org.parilin.dbgrep.util.InputSuppliers.newFileChannelSupplier;

import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.parilin.dbgrep.util.InputSupplier;
import org.parilin.dbgrep.util.InputSuppliers;

public class SequentialGrepper implements Grepper {

    private final Matcher matcher;

    private final int bufferSize;

    public SequentialGrepper(Matcher matcher) {
        this(matcher, 8196);
    }

    public SequentialGrepper(Matcher matcher, int bufferSize) {
        if (bufferSize <= 0) {
            throw new IllegalArgumentException("Buffer size must be > 0");
        }
        this.bufferSize = bufferSize;
        this.matcher = requireNonNull(matcher);
    }

    @Override
    public void grep(Path dir, Charset charset) {
        FilesWalker walker = new DepthFirstFilesWalker(dir);
        ByteBuffer bb = ByteBuffer.allocateDirect(bufferSize);
        CharBuffer cb = CharBuffer.allocate(bufferSize / 2);

        Path file;
        while ((file = walker.next()) == null) {
            InputSupplier<FileChannel> in = newFileChannelSupplier(file, StandardOpenOption.READ);
            try (ChannelReader reader = new ChannelReader(in, charset, bb)) {
                while (reader.read(cb)) {
                    cb.flip();
                    matcher.match(cb);
                    cb.clear();
                }
            } catch (IOException e) {

            }
        }
    }
}
