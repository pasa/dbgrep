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
     * @param chankIndex index of the chunk
     * @param result result of matching
     * @param isFinalChunk whether this chunk is final.
     */
    void insertResult(Path file, long chankIndex, ChunkMatchResult result, boolean isFinalChunk);

    /**
     * Gets collected indexes of matches in whole file.
     *
     * @return match indexes
     */
    long[] getCollectedIndexes();

    void addException(Throwable t);
}
