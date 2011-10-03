package org.parilin.dbgrep;

import java.io.Closeable;
import java.io.IOException;
import java.nio.CharBuffer;

/**
 * Reader of something.
 */
public interface Reader extends Closeable {

    /**
     * Reads content to the given char buffer.
     *
     * @param buff buffer
     * @return <code>true</code> if reader can read further and <code>false</code> if content is over.
     * @throws IOException if something goes wrong
     */
    boolean read(CharBuffer buff) throws IOException;
}
