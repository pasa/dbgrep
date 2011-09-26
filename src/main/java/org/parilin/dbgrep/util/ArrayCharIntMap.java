package org.parilin.dbgrep.util;

import java.util.Arrays;
import java.util.Objects;

import javax.annotation.concurrent.Immutable;

/**
 * Map from <code>char </code>to <code>int</code>.
 * <p>
 * Uses array of ints for mapping but reduce the size of it according the lowest
 * character.
 */
public class ArrayCharIntMap implements CharIntMap {

    private final int[] array;

    private final char lowest;

    private final int defaultValue;

    /**
     * Creates map for the given pattern
     *
     * @param pattern
     *            pattern
     * @return new instance parameterized specifically for this pattern.
     */
    public static ArrayCharIntMap createForPattern(char[] pattern) {
	char min = Character.MAX_VALUE;
	char max = Character.MIN_VALUE;
	int length = pattern.length;
	for (int i = 0; i < length; i++) {
	    char ch = pattern[i];
	    max = max > ch ? max : ch;
	    min = min < ch ? min : ch;
	}
	return new ArrayCharIntMap(max - min + 1, min, length);
    }

    /**
     * Constructor for CharIntMap.
     *
     * @param size
     *            the size of the array
     * @param lowest
     *            the lowest occurring character
     * @param defaultValue
     *            a default value to initialize the underlying <code>int</code>
     *            array with
     */
    public ArrayCharIntMap(int size, char lowest, int defaultValue) {
	this(initArray(size, defaultValue), lowest, defaultValue);
    }

    /*
     * init method for array to use in in the this() call.
     */
    private static int[] initArray(int size, int defaultValue) {
	int[] result = new int[size];
	if (defaultValue != 0) {
	    Arrays.fill(result, defaultValue);
	}
	return result;
    }

    /*
     * Private constructor for internal use.
     */
    private ArrayCharIntMap(int[] array, char lowest, int defaultValue) {
	this.lowest = lowest;
	this.defaultValue = defaultValue;
	this.array = array;
    }

    @Override
    public final int get(char c) {
	char x = (char) (c - lowest);
	if (x >= array.length) {
	    return defaultValue;
	}
	return array[x];
    }

    @Override
    public final void put(char c, int val) {
	char x = (char) (c - lowest);
	if (x >= array.length) {
	    return;
	}
	array[x] = val;
    }

    @Override
    public ArrayCharIntMap makeCopy() {
	return new ArrayCharIntMap(Arrays.copyOf(array, array.length), lowest,
		defaultValue);
    }

    /**
     * Converts this map to immutable one.
     *
     * @return immutable copy of this map.
     */
    public ImmutableCharIntMap toImmutable() {
	return new ImmutableWrapper(this);
    }

    @Immutable
    private static final class ImmutableWrapper implements ImmutableCharIntMap {

	private final CharIntMap mutable;

	private ImmutableWrapper(CharIntMap mutable) {
	    this.mutable = Objects.requireNonNull(mutable).makeCopy();
	}

	@Override
	public int get(char c) {
	    return mutable.get(c);
	}

    }
}
