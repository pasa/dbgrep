package org.parilin.dbgrep;

import static java.util.Objects.requireNonNull;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Grepper which divide IO and pattern matching processes to separate thread pools (stages).
 * <p>
 * See <a href="http://en.wikipedia.org/wiki/Staged_event-driven_architecture">SEDA</a> for additional information.
 */
public class StagedGrepper implements Grepper {

    private final ExecutorService fileReadStage;

    private final ExecutorService matchStage;

    private final MatcherFactory matcherFactory;

    private final int bufferSize;

    public StagedGrepper(int fileReadStageSize, int matchStageSize, MatcherFactory matcherFactory, int bufferSize) {
        if (bufferSize <= 0) {
            throw new IllegalArgumentException("Buffer size must be > 0");
        }
        this.matcherFactory = requireNonNull(matcherFactory);
        if (fileReadStageSize <= 0) {
            throw new IllegalArgumentException("fileReadStageSize must be > 0");
        }
        if (matchStageSize <= 0) {
            throw new IllegalArgumentException("matchStageSize must be > 0");
        }
        this.bufferSize = bufferSize;
        this.fileReadStage = Executors.newFixedThreadPool(fileReadStageSize);
        this.matchStage = Executors.newFixedThreadPool(matchStageSize);
    }

    @Override
    public void grep(Path dir, char[] needle, Charset charset, ResultsCollector collector)
                    throws InterruptedException {
        // TODO Auto-generated method stub

    }

}
