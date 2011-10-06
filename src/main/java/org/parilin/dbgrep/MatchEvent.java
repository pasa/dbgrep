package org.parilin.dbgrep;

import java.nio.CharBuffer;
import java.nio.file.Path;

public class MatchEvent {
    // poison event
    static final MatchEvent POISON = new MatchEvent(null, -1, null, true);

    final Path file;

    final long chunkIndex;

    final CharBuffer chunk;

    final boolean isFinalChunk;


    public MatchEvent(Path file, long chunkIndex, CharBuffer chunk, boolean isFinalChunk) {
        this.file = file;
        this.chunkIndex = chunkIndex;
        this.chunk = chunk;
        this.isFinalChunk = isFinalChunk;
    }
}