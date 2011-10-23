package org.parilin.dbgrep;

import static org.parilin.dbgrep.util.InputSuppliers.newFileChannelSupplier;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.parilin.dbgrep.util.InputSupplier;

/**
 * This concurrent grepper use work stealing policy to balance work among threads.
 * <p>
 * Each thread divide grep process onto file reading task and matching task and executes this tasks alternately
 * according circumstances.
 */
public class WorkStealingGrepper extends TaskableGrepper {

    static class WorkStealingGrepperTask implements Runnable {

        private static final int MAX_SEQUENTIAL_READS = 5;

        private final AtomicInteger tasksCounter;

        private final Matcher matcher;

        private final FilesWalker walker;

        private final Charset charset;

        private final ResultsCollector collector;

        private final ResultsMerger merger;

        private final int bufferSize;

        private final TransferQueue<MatchEvent> eventQueue;

        public WorkStealingGrepperTask(Matcher matcher, FilesWalker walker, Charset charset,
                        ResultsCollector collector, ResultsMerger merger, int bufferSize,
                        TransferQueue<MatchEvent> eventQueue, AtomicInteger tasksCounter) {
            this.matcher = matcher;
            this.walker = walker;
            this.charset = charset;
            this.collector = collector;
            this.merger = merger;
            this.bufferSize = bufferSize;
            this.eventQueue = eventQueue;
            this.tasksCounter = tasksCounter;
        }

        @Override
        public void run() {
            tasksCounter.incrementAndGet();
            CharsetDecoder decoder = charset.newDecoder();
            ByteBuffer bb = ByteBuffer.allocateDirect(bufferSize);
            for (;;) {
                if (doMatchWork()) {
                    // all the work have done
                    return;
                }

                Path file = walker.next();
                if (file == null) {
                    break; // no more files.
                }
                InputSupplier<FileChannel> in = newFileChannelSupplier(file, StandardOpenOption.READ);
                int sequentialReadCount = 0;
                // do #MAX_SEQUENTIAL_READS reads and try doMatchWork again
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
                        eventQueue.offer(new MatchEvent(file, chunk++, cb.asReadOnlyBuffer(), !further));
                        // do MAX_SEQUENTIAL_READS reads in a row to reduce contention on the event queue
                        if (sequentialReadCount == MAX_SEQUENTIAL_READS) {
                            sequentialReadCount = 0;
                            if (doMatchWork()) { // do match work
                                // all the work have done
                                return;
                            }
                        } else {
                            sequentialReadCount++;
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
            int tasks = tasksCounter.decrementAndGet();
            if (tasks == 0) { // this is a final task
                // kill'em all
                eventQueue.offer(MatchEvent.POISON);
            }
            while (!doMatchWork()) {
                // help other thread to process reminder of the queue
            }
        }

        /**
         * Do match work. return <code>true</code> if work is completely done and <code>false</code> if queue is empty
         * and need to read some files.
         */
        boolean doMatchWork() {
            for (;;) {
                MatchEvent event = eventQueue.poll();
                if (event == null) {
                    // there is not chunks to match. need do read work
                    return false;
                }
                if (event == MatchEvent.POISON) {
                    // return poison to queue. this notifies other match tasks to finish the work
                    eventQueue.offer(MatchEvent.POISON);
                    return true;
                }
                ChunkMatchResult matchResult = matcher.match(event.chunk);
                long[] matches = merger.merge(event.file, event.chunkIndex, matchResult, event.isFinalChunk);
                if (matches != null && matches.length != 0) {
                    collector.matches(event.file, matches);
                }
            }
        }
    }

    static class WorkStealingTaskStrategy implements GrepperTaskStrategy {

        private final AtomicInteger tasksCounter = new AtomicInteger();

        private ResultsMerger merger;

        private Charset charset;

        private ResultsCollector collector;

        private TransferQueue<MatchEvent> eventQueue = new LinkedTransferQueue<>();

        @Override
        public void init(Path dir, char[] needle, Charset charset, ResultsCollector collector) {
            this.charset = charset;
            this.collector = collector;
            merger = new ConcurrentResultsMerger(needle);
        }

        @Override
        public Runnable createTask(FilesWalker walker, Matcher matcher, int bufferSize) {
            return new WorkStealingGrepperTask(matcher, walker, charset, collector, merger, bufferSize, eventQueue,
                            tasksCounter);
        }

    }

    public WorkStealingGrepper(int threads, MatcherFactory matcherFactory) {
        this(threads, matcherFactory, 8196);
    }

    public WorkStealingGrepper(int threads, MatcherFactory matcherFactory, int bufferSize) {
        super(new WorkStealingTaskStrategy(), threads, matcherFactory, bufferSize);
    }

}
