package org.parilin.dbgrep.util;

import java.io.IOException;

/**
 * Supplier interface for input sources.
 */
public interface InputSupplier<T> {

    T getInput() throws IOException;
}
