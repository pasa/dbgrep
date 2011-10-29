package org.parilin.dbgrep;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;

/**
 * Grepper which executed in the same thread sequentially.
 */
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
    public void grep(Path dir, char[] needle, Charset charset, ResultsCollector collector)
                    throws InterruptedException {
        Matcher matcher = matcherFactory.create(needle);
        ResultsMerger merger = new SequentialResultsMerger(needle);
        try (FilesWalker walker = new DepthFirstFilesWalker(dir)) {
            SequentialGrepperTask task =
                new SequentialGrepperTask(matcher, walker, charset, collector, merger, bufferSize);
            task.run();
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
        } catch (IOException e) {
            collector.exception(e);
        }
    }

    @Override
    public String toString() {
        return "SequentialGrepper";
    }
}
