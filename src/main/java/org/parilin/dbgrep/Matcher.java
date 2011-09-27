package org.parilin.dbgrep;

import java.nio.CharBuffer;

/**
 * Represents search algorithm
 */
public interface Matcher {

    /**
     * Made search in the chunk of data.
     *
     * @param chunk chunk of data
     * @return result of the search
     */
    ChunkMatchResult match(CharBuffer chunk);
}
