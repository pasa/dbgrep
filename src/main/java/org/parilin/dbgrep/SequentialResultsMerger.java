package org.parilin.dbgrep;

import static org.parilin.dbgrep.util.MergerUtil.mergeCompletedChunks;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * Sequential implementation.
 * <p>
 * Assumes that all chunks for a file come in sequential ascending order.
 */
@NotThreadSafe
public class SequentialResultsMerger implements ResultsMerger {

    private final Map<Path, List<ChunkMatchResult>> results = new HashMap<>();

    private final char[] needle;

    public SequentialResultsMerger(char[] needle) {
        this.needle = needle;
    }

    @Override
    public long[] merge(Path file, long chankIndex, ChunkMatchResult result, boolean isFinalChunk) {
        List<ChunkMatchResult> list = getList(file);
        list.add(result);
        if (!isFinalChunk) {
            return null;
        }
        results.remove(file); // all merged
        return mergeCompletedChunks(list, needle);
    }

    private List<ChunkMatchResult> getList(Path file) {
        List<ChunkMatchResult> list = results.get(file);
        if (list == null) {
            list = new LinkedList<>(); // will not acces by index
            results.put(file, list);
        }
        return list;
    }
}
