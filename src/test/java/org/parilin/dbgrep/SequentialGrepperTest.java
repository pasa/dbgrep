package org.parilin.dbgrep;

import org.junit.Test;

public class SequentialGrepperTest {

    @Test
    public void testGrep() throws InterruptedException {
        GrepperTestUtil.grepperTest(new SequentialGrepper(BoyerMooreHorspoolMatcher.FACTORY));
    }
}
