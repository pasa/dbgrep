package org.parilin.dbgrep;

import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import org.junit.Test;

public class SequentialGrepperTest {

    @Test
    public void testGrep() {
        String pattern = "Free Software Foundation";
        SequentialGrepper grepper = new SequentialGrepper(BoyerMooreHorspoolMatcher.PROVIDER);
        Charset charset = Charset.forName("UTF-8");
        Path dir = FileSystems.getDefault().getPath("test_data", "licenses", "gpl.txt");
        grepper.grep(dir, pattern.toCharArray(), charset, new ResultsCollector() {

            @Override
            public void matches(Path file, long[] matches) {
                // TODO Auto-generated method stub

            }

            @Override
            public void exception(Throwable t) {
                t.printStackTrace();

            }

        });
    }

}
