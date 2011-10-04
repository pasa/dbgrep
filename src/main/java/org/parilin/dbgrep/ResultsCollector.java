package org.parilin.dbgrep;

import java.nio.file.Path;

/**
 * Interface for results collector for one file.
 */
public interface ResultsCollector {

    /**
     * Inserts chunk results with appropriate index.
     *
     * @param file file
     * @param matches match indexes
     */
    void matches(Path file, long[] matches);

    /**
     * Handles exceptions.
     *
     * @param t exception
     */
    void exception(Throwable t);
}
