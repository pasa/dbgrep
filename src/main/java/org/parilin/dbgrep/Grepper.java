package org.parilin.dbgrep;

import java.nio.charset.Charset;
import java.nio.file.Path;

public interface Grepper {

    void grep(Path dir, Charset charset, ResultsCollector collector);
}
