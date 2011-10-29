package org.parilin.dbgrep;

import static java.util.Objects.requireNonNull;
import static org.parilin.dbgrep.util.InputSuppliers.newFileChannelSupplier;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.parilin.dbgrep.util.ConcurrentUtil;
import org.parilin.dbgrep.util.InputSupplier;

/**
 * Grepper which divide IO and pattern matching processes to separate thread pools (stages).
 * <p>
 * See <a href="http://en.wikipedia.org/wiki/Staged_event-driven_architecture">SEDA</a> for additional information.
 */
public class StagedGrepper implements Grepper {

    private final ExecutorService fileReadStage;

    private final ExecutorService matchStage;

    private final MatcherFactory matcherFactory;

    private final TransferQueue<MatchEvent> matchQueue = new LinkedTransferQueue<>();

    private final int bufferSize;

    private final int fileReadStageSize;

    private final int matchStageSize;

    public StagedGrepper(int fileReadStageSize, int matchStageSize, MatcherFactory matcherFactory) {
        this(fileReadStageSize, matchStageSize, matcherFactory, 8196);
    }

    public StagedGrepper(int fileReadStageSize, int matchStageSize, MatcherFactory matcherFactory, int bufferSize) {
        this.fileReadStageSize = fileReadStageSize;
        this.matchStageSize = matchStageSize;
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
        Matcher matcher = matcherFactory.create(needle);
        ResultsMerger merger = new ConcurrentResultsMerger(needle);
        List<Future<?>> futures = new ArrayList<>(fileReadStageSize + matchStageSize);
        AtomicInteger readTasksCounter = new AtomicInteger(0); // need for correct tasks finalizing
        try (FilesWalker walker = new ConcurrentFilesWalker(new DepthFirstFilesWalker(dir))) {
            for (int i = 0; i < matchStageSize; i++) {
                futures.add(matchStage.submit(new MatchTask(matcher, merger, collector)));
            }
            for (int i = 0; i < fileReadStageSize; i++) {
                futures.add(fileReadStage.submit(new ReadTask(walker, charset, collector, readTasksCounter)));
            }
            try {
                ConcurrentUtil.waitAll(futures, collector);
            } catch (InterruptedException e) {
                matchStage.shutdownNow();
                fileReadStage.shutdownNow();
                throw e;
            }
        } catch (IOException e) {
            collector.exception(e);
        }
    }

    /*
     * Reads data from files
     */
    class ReadTask implements Runnable {

        private final AtomicInteger readTaskCounter;

        private final FilesWalker walker;

        private final Charset charset;

        private final ResultsCollector collector;

        public ReadTask(FilesWalker walker, Charset charset, ResultsCollector collector,
                        AtomicInteger readTaskCounter) {
            this.walker = walker;
            this.charset = charset;
            this.collector = collector;
            this.readTaskCounter = readTaskCounter;
        }

        @Override
        public void run() {
            readTaskCounter.incrementAndGet();
            ByteBuffer bb = ByteBuffer.allocateDirect(bufferSize);
            Path file;
            while ((file = walker.next()) != null) {
                InputSupplier<FileChannel> in = newFileChannelSupplier(file, StandardOpenOption.READ);
                CharsetDecoder decoder = charset.newDecoder();
                try (ChannelReader reader = new ChannelReader(in, decoder, bb)) {
                    long chunk = 0;
                    for (;;) {
                        if (Thread.currentThread().isInterrupted()) {
                            // not reset interruption flag. interruption will be handled up on stack
                            return;
                        }
                        CharBuffer cb = CharBuffer.allocate((int) (bufferSize * decoder.averageCharsPerByte()));
                        boolean further = reader.read(cb);
                        cb.flip();
                        matchQueue.offer(new MatchEvent(file, chunk++, cb.asReadOnlyBuffer(), !further));
                        if (!further) {
                            break;
                        }
                    }
                } catch (IOException e) {
                    collector.exception(e);
                }
            }
            int tasks = readTaskCounter.decrementAndGet();
            if (tasks == 0) { // this is a final task
                // finish them!!!
                matchQueue.offer(MatchEvent.POISON);
            }
        }
    }

    /**
     * Matches data with a matcher
     */
    class MatchTask implements Runnable {

        private final Matcher matcher;

        private final ResultsMerger merger;

        private final ResultsCollector collector;

        public MatchTask(Matcher matcher, ResultsMerger merger, ResultsCollector collector) {
            this.matcher = matcher;
            this.merger = merger;
            this.collector = collector;
        }

        @Override
        public void run() {
            for (;;) {
                try {
                    MatchEvent event = matchQueue.take();
                    if (event == MatchEvent.POISON) {
                        // return poison to queue. this notifies other match tasks to finish the work
                        matchQueue.offer(MatchEvent.POISON);
                        break;
                    }
                    ChunkMatchResult matchResult = matcher.match(event.chunk);
                    long[] matches = merger.merge(event.file, event.chunkIndex, matchResult, event.isFinalChunk);
                    if (matches != null && matches.length != 0) {
                        collector.matches(event.file, matches);
                    }
                } catch (InterruptedException e) {
                    // set interrupt status. interruption will be handled up on stack
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

    }

    @Override
    public String toString() {
        return "StagedGrepper [readThreads=" + fileReadStageSize + ", matchThreads=" + matchStageSize + "]";
    }
}
