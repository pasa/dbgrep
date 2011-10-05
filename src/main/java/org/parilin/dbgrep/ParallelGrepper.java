package org.parilin.dbgrep;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ParallelGrepper implements Grepper {

    private final MatcherFactory matcherFactory;

    private ExecutorService executor;

    private final int bufferSize;

    private final int threads;

    public ParallelGrepper(int threads, MatcherFactory matcherFactory, int bufferSize) {
        this.matcherFactory = Objects.requireNonNull(matcherFactory);
        if (bufferSize <= 0) {
            throw new IllegalArgumentException("Buffer size must be > 0");
        }
        this.bufferSize = bufferSize;
        if (threads <= 0) {
            throw new IllegalArgumentException("threads must be > 0");
        }
        if (threads > 1) {
            executor = Executors.newFixedThreadPool(threads - 1); // one thread will be main
        }
        this.threads = threads;
    }

    @Override
    public void grep(Path dir, char[] needle, Charset charset, ResultsCollector collector)
                    throws InterruptedException {
        Matcher matcher = matcherFactory.create(needle);
        List<Future<?>> futures = new ArrayList<>(threads - 1);
        try (FilesWalker walker = new ConcurrentFilesWalker(new DepthFirstFilesWalker(dir))) {
            for (int i = 0; i < threads - 1; i++) {
                SequentialGrepperTask task =
                    new SequentialGrepperTask(matcher, needle, walker, charset, collector, bufferSize);
                futures.add(executor.submit(task));
            }
            SequentialGrepperTask task =
                new SequentialGrepperTask(matcher, needle, walker, charset, collector, bufferSize);
            // last task executed in the current thread
            task.run();
            // main task is finished
            if (Thread.interrupted()) {
                // interrupt other
                if (threads > 1) {
                    executor.shutdownNow();
                }
                throw new InterruptedException();
            }
            for (Future<?> future : futures) {
                try {
                    future.get();
                } catch (ExecutionException e) {
                    collector.exception(e.getCause());
                } catch (InterruptedException e) {
                    executor.shutdownNow();
                    throw e;
                }
            }
        } catch (IOException e) {
            collector.exception(e);
        }
    }

}
