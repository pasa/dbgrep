package org.parilin.dbgrep;

import org.junit.Test;

public class ParallelGrepperTest {
    @Test
    public void testGrep() throws InterruptedException {
        GrepperTestUtil.grepperTest(new ParallelGrepper(2, BoyerMooreHorspoolMatcher.FACTORY));
    }
}
