package org.parilin.dbgrep.util;

import static java.util.Arrays.binarySearch;
import static org.parilin.dbgrep.util.ArrayUtil.longArray;

import java.util.ArrayList;
import java.util.List;

import org.parilin.dbgrep.ChunkMatchResult;

public final class MergerUtil {

    private MergerUtil() {
        throw new UnsupportedOperationException();
    }

    public static long[] mergeCompletedChunks(List<ChunkMatchResult> chunks, char[] needle) {
        ArrayList<Long> result = new ArrayList<>();
        long totalChunkIndex = 0;
        int[] lastPrefixMatches = new int[0];
        for (ChunkMatchResult match : chunks) {
            int[] suffixMatches = match.suffixMatches();
            if (lastPrefixMatches.length != 0) {
                for (int prefLen : lastPrefixMatches) {
                    int suffixLenght = needle.length - prefLen;
                    if (binarySearch(suffixMatches, suffixLenght) >= 0) { // assume that results sorted
                        // match found at chunks boundaries
                        result.add(totalChunkIndex - prefLen);
                    }
                }
            }
            for (int perfect : match.perfectMatches()) {
                result.add(totalChunkIndex + perfect);
            }
            lastPrefixMatches = match.prefixMatches();
        }
        return longArray(result);
    }

}
