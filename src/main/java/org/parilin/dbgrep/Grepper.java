package org.parilin.dbgrep;

import java.nio.charset.Charset;
import java.nio.file.Path;

public interface Grepper {

    void grep(Path dir, char[] needle, Charset charset, ResultsCollector collector);
}
