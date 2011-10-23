package org.parilin.dbgrep;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public final class GrepperTestUtil {

    static class TestResultCollector implements ResultsCollector {

        private volatile Path file;

        private volatile long[] matches;

        @Override
        public void matches(Path file, long[] matches) {
            this.file = file;
            this.matches = matches;
        }

        @Override
        public void exception(Throwable t) {
        }

        public void assertResult(Path file, long[] matches) {
            assertEquals(file, this.file);
            assertArrayEquals(matches, this.matches);
        }
    }

    public GrepperTestUtil() {
        throw new UnsupportedOperationException();
    }

    public static void grepperTest(Grepper grepper) throws InterruptedException {
        String pattern = "Free Software Foundation";
        Charset charset = Charset.forName("UTF-8");
        FileSystem fs = FileSystems.getDefault();
        Path dir = fs.getPath("test_data", "licenses");
        GrepperTestUtil.TestResultCollector collector = new GrepperTestUtil.TestResultCollector();
        grepper.grep(dir, pattern.toCharArray(), charset, collector);
        Path gpl = fs.getPath("test_data", "licenses", "gpl.txt");
        long[] expectedMatches = {115, 750, 29562, 30290, 33302};
        collector.assertResult(gpl, expectedMatches);
    }
}
