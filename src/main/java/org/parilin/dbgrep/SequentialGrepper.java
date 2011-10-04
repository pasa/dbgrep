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

    private final MatcherFactory matcherFactory;

    private final int bufferSize;

    public SequentialGrepper(MatcherFactory matcherFactory) {
        this(matcherFactory, 8196);
    }

    public SequentialGrepper(MatcherFactory matcherFactory, int bufferSize) {
        if (bufferSize <= 0) {
            throw new IllegalArgumentException("Buffer size must be > 0");
        }
        this.bufferSize = bufferSize;
        this.matcherFactory = requireNonNull(matcherFactory);
    }

    @Override
    public void grep(Path dir, char[] needle, Charset charset, ResultsCollector collector) {
        float averageCharsPerByte = charset.newDecoder().averageCharsPerByte();
        Matcher matcher = matcherFactory.create(needle);
        FilesWalker walker = new DepthFirstFilesWalker(dir);
        ByteBuffer bb = ByteBuffer.allocateDirect(bufferSize);
        CharBuffer cb = CharBuffer.allocate((int) (bufferSize * averageCharsPerByte));
        ResultsMerger merger = new SequentialResultsMerger(needle);
        Path file;
        while ((file = walker.next()) != null) {
            InputSupplier<FileChannel> in = newFileChannelSupplier(file, StandardOpenOption.READ);
            try (ChannelReader reader = new ChannelReader(in, charset, bb)) {
                long chunk = 0;
                for (;;) {
                    boolean further = reader.read(cb);
                    cb.flip();
                    ChunkMatchResult matchResult = matcher.match(cb);
                    long[] matches = merger.merge(file, chunk++, matchResult, !further);
                    if (matches != null) {
                        collector.matches(file, matches);
                    }
                    if (!further) {
                        break;
                    }
                    cb.clear();
                }
            } catch (IOException e) {
                collector.exception(e);
            }
        }
    }
}
