package org.parilin.dbgrep;

import static java.util.Objects.requireNonNull;
import static org.parilin.dbgrep.util.InputSuppliers.newFileChannelSupplier;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.parilin.dbgrep.util.InputSupplier;

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
    public void grep(Path dir, Charset charset, ResultsCollector collector) {
        float averageCharsPerByte = charset.newDecoder().averageCharsPerByte();
        FilesWalker walker = new DepthFirstFilesWalker(dir);
        ByteBuffer bb = ByteBuffer.allocateDirect(bufferSize);
        CharBuffer cb = CharBuffer.allocate((int) (bufferSize * averageCharsPerByte));

        Path file;
        while ((file = walker.next()) != null) {
            StringBuilder sb = new StringBuilder();
            InputSupplier<FileChannel> in = newFileChannelSupplier(file, StandardOpenOption.READ);
            try (ChannelReader reader = new ChannelReader(in, charset, bb)) {
                long chunk = 0;
                for (;;) {
                    boolean further = reader.read(cb);
                    cb.flip();
                    ChunkMatchResult matchResult = matcher.match(cb);
                    collector.insertResult(file, chunk++, matchResult, !further);
                    sb.append(cb);
                    if (!further) {
                        System.out.println(chunk - 1);
                        break;
                    }
                    cb.clear();
                }
            } catch (IOException e) {
                collector.addException(e);
            }
        }
    }
}
