package org.parilin.dbgrep;

import org.junit.Test;

public class StagedGrepperTest {

    @Test
    public void testGrep() throws InterruptedException {
        GrepperTestUtil.grepperTest(new StagedGrepper(2, 2, BoyerMooreHorspoolMatcher.FACTORY));
    }

}
