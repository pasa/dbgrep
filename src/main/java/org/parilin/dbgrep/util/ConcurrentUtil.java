package org.parilin.dbgrep.util;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.parilin.dbgrep.ResultsCollector;

/**
 * Some utils for concurrent.
 */
public final class ConcurrentUtil {

    private ConcurrentUtil() {
        throw new UnsupportedOperationException();
    }

    public static void waitAll(List<Future<?>> futures, ResultsCollector collector) throws InterruptedException {
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                collector.exception(e.getCause());
            }
        }
    }

}
