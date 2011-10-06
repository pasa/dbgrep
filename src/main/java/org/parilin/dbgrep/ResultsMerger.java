package org.parilin.dbgrep;

import java.nio.file.Path;

/**
 * Interface for chunk results merger.
 */
public interface ResultsMerger {

    /**
     * Merge given chunk result data to the others.
     * <p>
     * If results of all chunks are collected then returns array of match indexes otherwise return <code>null</code>.
     * <p>
     * Sequential implementations will return indexes after final chunk is merged. Concurrent implementation may do in
     * case when collect all chunk results.
     *
     * @param file file
     * @param chunkIndex index of the chunk
     * @param result results of the match
     * @param isFinalChunk indicates that chunk is final.
     * @return
     */
    long[] merge(Path file, long chunkIndex, ChunkMatchResult result, boolean isFinalChunk);
}
