package org.parilin.dbgrep.util;

import java.util.List;

import javax.annotation.Nullable;

/**
 * Array utilities.
 */
public final class ArrayUtil {

    private ArrayUtil() {
        throw new UnsupportedOperationException();
    }

    /**
     * Convert list of numbers to int array.
     *
     * @param list source list
     * @return int array with unboxed values and <code>null</code> if source list is null.
     */
    @Nullable
    public static <N extends Number> int[] intArray(@Nullable List<N> list) {
        if (list == null) {
            return null;
        }
        int[] result = new int[list.size()];
        int index = 0;
        for (N n : list) {
            result[index++] = n.intValue();
        }
        return result;
    }

    /**
     * Convert list of numbers to long array.
     *
     * @param list source list
     * @return long array with unboxed values and <code>null</code> if source list is null.
     */
    @Nullable
    public static <N extends Number> long[] longArray(@Nullable List<N> list) {
        if (list == null) {
            return null;
        }
        long[] result = new long[list.size()];
        int index = 0;
        for (N n : list) {
            result[index++] = n.longValue();
        }
        return result;
    }
}
