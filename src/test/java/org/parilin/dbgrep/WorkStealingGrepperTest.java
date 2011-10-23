package org.parilin.dbgrep;

import org.junit.Test;

public class WorkStealingGrepperTest {
    @Test
    public void testGrep() throws InterruptedException {
        GrepperTestUtil.grepperTest(new WorkStealingGrepper(3, BoyerMooreHorspoolMatcher.FACTORY));
    }
}
