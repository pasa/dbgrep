package org.parilin.dbgrep;

import javax.annotation.concurrent.Immutable;

/**
 * Represents immutable interface for char int map.
 */
@Immutable
public interface ImmutableCharIntMap {

    /**
     * Returns the stored value for the given <code>char</code>.
     *
     * @param c
     *            the <code>char</code>
     * @return the stored value
     */
    int get(char c);

}