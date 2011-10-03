package org.parilin.dbgrep;

import java.io.Closeable;
import java.nio.file.Path;

/**
 * A walker for file system.
 */
public interface FilesWalker extends Closeable {

    /**
     * Returns next file path.
     *
     * @return next path if exists and <code>null</code> otherwise
     */
    Path next();
}