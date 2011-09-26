package org.parilin.dbgrep;

/**
 * Match results for the chunk of data.
 */
public class ChunkMatchResult {

    private final int[] suffixMatches;

    private final int[] perfectMatches;

    private final int[] prefixMatches;

    public ChunkMatchResult(int[] suffixMatches, int[] perfectMatches, int[] prefixMatches) {
        this.suffixMatches = suffixMatches;
        this.perfectMatches = perfectMatches;
        this.prefixMatches = prefixMatches;
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
}
