package org.parilin.dbgrep;

import static org.junit.Assert.assertNotNull;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import org.junit.Assert;
import org.junit.Test;

public class BoyerMooreHorspoolMatcherTest {

    @Test
    public void testMatch() throws UnsupportedEncodingException {
        char[] needle = "abc".toCharArray();
        ByteBuffer haystack = ByteBuffer.wrap("bbcabca".getBytes("UTF-16"));
        BoyerMooreHorspoolMatcher matcher = new BoyerMooreHorspoolMatcher(needle);
        ChunkMatchResult result = matcher.match(haystack);
        assertNotNull(result);
        Assert.assertArrayEquals(result.perfectMatches(), new int[] {3});
    }

}
