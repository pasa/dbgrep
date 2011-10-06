package org.parilin.dbgrep;

import java.nio.charset.Charset;
import java.nio.file.Path;

public interface GrepperTaskStrategy {

    void init(Path dir, char[] needle, Charset charset, ResultsCollector collector);

    Runnable createTask(FilesWalker walker, Matcher matcher, int bufferSize);
}
