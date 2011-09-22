package org.parilin.dbgrep;

/**
 * Mutable interface for char to int map.
 */
public interface CharIntMap extends ImmutableCharIntMap {

    /**
     * Sets the stored value for the given <code>char</code>.
     *
     * @param c
     *            the <code>char</code>
     * @param val
     *            the new value
     */
    void put(char c, int val);

    /**
     * Makes copy of this map.
     *
     * @return copy of the given map.
     */
    CharIntMap makeCopy();

}