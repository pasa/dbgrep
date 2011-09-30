package org.parilin.dbgrep;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Concurrent files walker.
 * <p>
 * Concurrently iterates through files according not thread safe delegate walker.
 * <p>
 * Class holds a concurrent blocking queue with prefetched file paths. If cache becomes empty then one thread takes a
 * role of the fetcher and others wait until it's work have done. When iteration is finished the fetcher thread spoil
 * the cache queue with predefined value {@link #POISON} to notify other threads to finish.
 */
@ThreadSafe
public class ConcurrentFilesWalker implements FilesWalker {

    private static final int DEFAULT_CACHE_SIZE = 20;

    private static final Path POISON = FileSystems.getDefault().getPath("poison.dbgrep");

    private final int cacheSize;

    private final BlockingQueue<Path> precached = new LinkedTransferQueue<>();

    private final Lock prefetchLock = new ReentrantLock();

    @GuardedBy("prefetchLock")
    private final FilesWalker walker;

    public ConcurrentFilesWalker(int cacheSize, FilesWalker walker) {
        this(walker, DEFAULT_CACHE_SIZE);
    }

    public ConcurrentFilesWalker(FilesWalker walker, int cacheSize) {
        this.walker = Objects.requireNonNull(walker);
        if (cacheSize < 0) {
            throw new IllegalArgumentException("Cache size must be > 0");
        }
        this.cacheSize = cacheSize;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path next() {
        Path result = precached.poll();
        for (;;) {
            if (result == POISON) { // traverse is finished
                return null;
            }
            if (result != null) {
                return result;
            } else {
                if (tryPrefetchPaths()) {
                    // iteration finished. spoil queue to notify other threads about it.
                    spoil();
                    // try to take result again
                    result = precached.poll();
                } else {
                    // this thread is not fetcher
                    try {
                        // wait fetcher work
                        result = precached.take();
                    } catch (InterruptedException e) {
                        // assume that wait process is not so long.
                        // move iteration handling up to stack
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }

    /*
     * Trying to prefetch paths. If return true then iteration is over and need to finish iteration.
     */
    private boolean tryPrefetchPaths() {
        if (prefetchLock.tryLock()) { // use try to reduce contention
            // lucky thread is a fetcher
            try {
                int i = 0;
                while (i < cacheSize) {
                    Path next = walker.next();
                    if (next == null) {
                        // we not spoil here because we need unlock first
                        return true;
                    } else {
                        precached.offer(next);
                    }
                    i++;
                }
            } finally {
                prefetchLock.unlock();
            }
        }
        return false;
    }

    private void spoil() {
        int counts = cacheSize - precached.size();
        if (counts <= 0) {
            return;
        }
        int i = 0;
        while (i < counts) {
            precached.offer(POISON);
            i++;
        }
    }
}
