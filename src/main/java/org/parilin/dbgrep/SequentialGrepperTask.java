package org.parilin.dbgrep;

import static org.parilin.dbgrep.util.InputSuppliers.newFileChannelSupplier;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.parilin.dbgrep.util.InputSupplier;

/**
 * Task of the sequential grep.
 */
public class SequentialGrepperTask implements Runnable {
    private final Matcher matcher;

    private final FilesWalker walker;

    private final Charset charset;

    private final ResultsCollector collector;

    private final ResultsMerger merger;

    private final int bufferSize;

    public SequentialGrepperTask(Matcher matcher, FilesWalker walker, Charset charset, ResultsCollector collector,
                    ResultsMerger merger, int bufferSize) {
        this.matcher = matcher;
        this.walker = walker;
        this.charset = charset;
        this.collector = collector;
        this.merger = merger;
        this.bufferSize = bufferSize;
    }

    @Override
    public void run() {
        float averageCharsPerByte = charset.newDecoder().averageCharsPerByte();
        ByteBuffer bb = ByteBuffer.allocateDirect(bufferSize);
        CharBuffer cb = CharBuffer.allocate((int) (bufferSize * averageCharsPerByte));
        Path file;
        while ((file = walker.next()) != null) {
            InputSupplier<FileChannel> in = newFileChannelSupplier(file, StandardOpenOption.READ);
            try (ChannelReader reader = new ChannelReader(in, charset, bb)) {
                long chunk = 0;
                for (;;) {
                    if (Thread.currentThread().isInterrupted()) {
                        // not reset interruption flag. interruption will be handled up on stack
                        return;
                    }
                    boolean further = reader.read(cb);
                    cb.flip();
                    ChunkMatchResult matchResult = matcher.match(cb.asReadOnlyBuffer());
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