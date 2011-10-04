package org.parilin.dbgrep;

import static java.util.Arrays.copyOf;
import static java.util.Objects.requireNonNull;
import static org.parilin.dbgrep.util.ArrayUtil.intArray;

import java.nio.CharBuffer;
import java.util.ArrayList;

import javax.annotation.concurrent.Immutable;

import org.parilin.dbgrep.util.ArrayCharIntMap;
import org.parilin.dbgrep.util.ImmutableCharIntMap;

@Immutable
public class BoyerMooreHorspoolMatcher implements Matcher {

    public final static MatcherProvider PROVIDER = new MatcherProvider() {

        @Override
        public Matcher provide(char[] needle) {
            return new BoyerMooreHorspoolMatcher(needle);
        }
    };

    private final char[] needle;

    private final ImmutableCharIntMap shifts;

    public BoyerMooreHorspoolMatcher(char[] pattern) {
        this.needle = copyOf(requireNonNull(pattern), pattern.length);
        ArrayCharIntMap map = ArrayCharIntMap.createForPattern(pattern);
        // create shift table for the given pattern
        int last = pattern.length - 1;
        for (int scan = 0; scan < last; scan++) {
            map.put(pattern[scan], last - scan);
        }
        shifts = map.toImmutable();
    }

    public ChunkMatchResult match(CharBuffer haystack) {
        int chunkSize = haystack.remaining();
        ArrayList<Integer> perfectMatches = new ArrayList<>();
        ArrayList<Integer> prefixMatches = new ArrayList<>();
        ArrayList<Integer> suffixMatches = new ArrayList<>();
        int haystackLast = haystack.limit() - 1;
        int pivot = 0; // set pivot to 0 to search all needle suffixes in haystack prefix
        int needleLen = needle.length;
        int needleLast = needleLen - 1;
        int pivotLimit = haystackLast + needleLast;
        while (pivot <= pivotLimit) {
            int pivotDiff = pivot - haystackLast;
            int haystackIndex;
            int needleIndex;
            if (pivotDiff > 0) { // search all needle prefix in haystack suffix
                haystackIndex = haystackLast;
                needleIndex = needleLast - pivotDiff;
            } else { // standard algorithm behavior
                haystackIndex = pivot;
                needleIndex = needleLast;
            }
            while (true) {
                char haychar = haystack.get(haystackIndex);
                if (needle[needleIndex] == haychar) {
                    if (needleIndex == 0) {
                        if (pivotDiff > 0) { // needle prefix match
                            prefixMatches.add(needleLen - pivotDiff);
                        } else { // perfect match
                            perfectMatches.add(haystackIndex);
                        }
                        pivot++;
                        break;
                    }
                    if (haystackIndex == 0) {
                        // haystack prefix contains needle suffix
                        suffixMatches.add(needleLen - needleIndex);
                        pivot++;
                        break;
                    }
                    haystackIndex--;
                    needleIndex--;
                } else {
                    if (needleIndex == needleLast) { // last char doesn't match.
                        pivot += shifts.get(haychar); // shift according table
                    } else { // other char doesn't match
                        pivot++; // shift one
                    }
                    break; // restart
                }
            }
        }
        return new ChunkMatchResult(chunkSize, intArray(suffixMatches), intArray(perfectMatches),
                        intArray(prefixMatches));
    }
}
