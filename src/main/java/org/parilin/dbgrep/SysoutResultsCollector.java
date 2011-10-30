package org.parilin.dbgrep;

import java.nio.file.Path;
import java.util.Arrays;

public class SysoutResultsCollector implements ResultsCollector {

    @Override
    public void matches(Path file, long[] matches) {
        System.out.println(file + " " + Arrays.toString(matches));
    }

    @Override
    public void exception(Throwable t) {
        t.printStackTrace();
    }
}
