package org.parilin.dbgrep;

import java.util.Arrays;

/**
 * Match results for the chunk of data.
 */
public class ChunkMatchResult {

    private final int chunkSize;

    private final int[] suffixMatches;

    private final int[] perfectMatches;

    private final int[] prefixMatches;

    public ChunkMatchResult(int chunkSize, int[] suffixMatches, int[] perfectMatches, int[] prefixMatches) {
        this.chunkSize = chunkSize;
        this.suffixMatches = suffixMatches;
        this.perfectMatches = perfectMatches;
        this.prefixMatches = prefixMatches;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    /**
     * Returns lengths of the needle suffixes which are matched with haystack prefix.
     *
     * @return sorted in ascending order lengths of matched suffixes
     */
    public int[] suffixMatches() {
        return suffixMatches;
    }

    /**
     * Returns indexes of the perfect matches of the needle in the haystack.
     *
     * @return sorted in ascending order indexes of the perfect matches
     */
    public int[] perfectMatches() {
        return perfectMatches;
    }

    /**
     * Returns lengths of the needle prefixes which are matched with haystack suffix.
     *
     * @return sorted in descending order lengths if the matched prefixes
     */
    public int[] prefixMatches() {
        return prefixMatches;
    }

    public boolean isEmpty() {
        return suffixMatches.length == 0 && perfectMatches.length == 0 && prefixMatches.length == 0;
    }

    @Override
    public String toString() {
        return "ChunkMatchResult [suffixMatches=" + Arrays.toString(suffixMatches) + ", perfectMatches="
            + Arrays.toString(perfectMatches) + ", prefixMatches=" + Arrays.toString(prefixMatches) + "]";
    }

}
