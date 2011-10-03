package org.parilin.dbgrep;

import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import org.junit.Test;

public class SequentialGrepperTest {

    @Test
    public void testGrep() {
        String pattern = "Free Software Foundation";
        BoyerMooreHorspoolMatcher matcher = new BoyerMooreHorspoolMatcher(pattern.toCharArray());
        SequentialGrepper grepper = new SequentialGrepper(matcher);
        Charset charset = Charset.forName("UTF-8");
        Path dir = FileSystems.getDefault().getPath("test_data", "licenses", "gpl.txt");
        grepper.grep(dir, charset, new ResultsCollector() {

            @Override
            public void insertResult(Path file, long chankIndex, ChunkMatchResult result, boolean isFinalChunk) {
                if (!result.isEmpty()) {
                    System.out.println(chankIndex);
                    System.out.println(result);
                }
            }

            @Override
            public long[] getCollectedIndexes() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public void addException(Throwable t) {
                t.printStackTrace();

            }
        });
    }

}
