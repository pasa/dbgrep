package org.parilin.dbgrep;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;

import java.io.UnsupportedEncodingException;
import java.nio.CharBuffer;

import org.junit.Assert;
import org.junit.Test;

public class BoyerMooreHorspoolMatcherTest {

    @Test
    public void testMatchSimple() throws UnsupportedEncodingException {
        String haystack = "ccbcaabccaa";
        String needle = "aabcc";
        BoyerMooreHorspoolMatcher matcher = new BoyerMooreHorspoolMatcher(needle.toCharArray());
        ChunkMatchResult result = matcher.match(CharBuffer.wrap(haystack.toCharArray()).asReadOnlyBuffer());
        assertNotNull(result);
        assertArrayEquals(result.perfectMatches(), new int[] {4});
        assertArrayEquals(result.prefixMatches(), new int[] {2, 1});
        assertArrayEquals(result.suffixMatches(), new int[] {1, 2});
    }

    @Test
    public void testMatchRepeat() throws UnsupportedEncodingException {
        String haystack = "aaaaaaaa";
        String needle = "aaaa";
        BoyerMooreHorspoolMatcher matcher = new BoyerMooreHorspoolMatcher(needle.toCharArray());
        ChunkMatchResult result = matcher.match(CharBuffer.wrap(haystack.toCharArray()).asReadOnlyBuffer());
        assertNotNull(result);
        Assert.assertArrayEquals(result.perfectMatches(), new int[] {0, 1, 2, 3, 4});
        Assert.assertArrayEquals(result.prefixMatches(), new int[] {3, 2, 1});
        Assert.assertArrayEquals(result.suffixMatches(), new int[] {1, 2, 3});
    }
}
