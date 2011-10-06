package org.parilin.dbgrep;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.concurrent.ThreadSafe;

import org.parilin.dbgrep.util.MergerUtil;

/**
 * Concurrent implementation of the merger.
 * <p>
 * This merger can accept chunk results in random. Assumes that if merge method invoked with isFinalCunk = true the
 * corresponding chunk index is greater then other for this file.
 */
@ThreadSafe
public class ConcurrentResultsMerger implements ResultsMerger {

    private final ConcurrentHashMap<Path, FileEntry> entries = new ConcurrentHashMap<>();

    private final char[] needle;

    public ConcurrentResultsMerger(char[] needle) {
        this.needle = needle;
    }

    @Override
    public long[] merge(Path file, long chunkIndex, ChunkMatchResult result, boolean isFinalChunk) {
        FileEntry entry = entries.get(file);
        if (entry != null) { // check and put to reduce false object creation
            FileEntry newEntry = new FileEntry();
            entry = entries.putIfAbsent(file, newEntry);
            if (entry == null) {
                entry = newEntry;
            }
        }
        if (entry.addChunk(new Chunk(chunkIndex, result), isFinalChunk)) {
            // all chunks for file is collected and may be merged
            entries.remove(file);
            List<Chunk> chunks = new ArrayList<>(entry.chunks);
            Collections.sort(chunks, Chunk.INDEX_COMPARATOR);
            List<ChunkMatchResult> results = new ArrayList<>(chunks.size());
            for (Chunk chunk : chunks) {
                results.add(chunk.result);
            }
            return MergerUtil.mergeCompletedChunks(results, needle);
        } else {
            return null;
        }
    }

    static class Chunk {

        public static final Comparator<Chunk> INDEX_COMPARATOR = new Comparator<Chunk>() {

            @Override
            public int compare(Chunk o1, Chunk o2) {
                return Long.compare(o1.chunkIndex, o2.chunkIndex);
            }
        };

        final long chunkIndex;

        final ChunkMatchResult result;

        public Chunk(long chunkIndex, ChunkMatchResult result) {
            this.chunkIndex = chunkIndex;
            this.result = result;
        }

    }

    static class FileEntry {

        volatile long maxChunk = -1;

        AtomicLong chunksCount = new AtomicLong(0);

        final Queue<Chunk> chunks = new ConcurrentLinkedQueue<>();

        /*
         * Return true is all chunks is collected.
         */
        boolean addChunk(Chunk chunk, boolean isFinal) {
            chunks.offer(chunk);
            if (isFinal) {
                maxChunk = chunk.chunkIndex;
            }
            long collected = chunksCount.incrementAndGet();
            return collected == maxChunk;
        }
    }
}
