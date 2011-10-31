package org.parilin.dbgrep.benchmark;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.parilin.dbgrep.BoyerMooreHorspoolMatcher;
import org.parilin.dbgrep.Grepper;
import org.parilin.dbgrep.ParallelGrepper;
import org.parilin.dbgrep.SequentialGrepper;
import org.parilin.dbgrep.StagedGrepper;
import org.parilin.dbgrep.WorkStealingGrepper;

import com.google.caliper.Param;
import com.google.caliper.SimpleBenchmark;

public class MainBenchmark extends SimpleBenchmark {

    @BeforeClass
    public static void beforeClass() throws IOException {
        BenchmarkUtil.prepareTestData();
    }

    @Param({"seq", "par1", "par2", "par3", "par4", "staged11", "staged12", "staged21", "staged22", "staged13",
        "staged31", "steal1", "steal2", "steal3", "steal4"})
    String grepperType;

    private static Grepper createGrepper(String grepperType) {
        if (grepperType.equals("seq")) {
            return new SequentialGrepper(BoyerMooreHorspoolMatcher.FACTORY);
        }
        if (grepperType.startsWith("par")) {
            return new ParallelGrepper(charToInt(grepperType.charAt(3)), BoyerMooreHorspoolMatcher.FACTORY);
        }
        if (grepperType.startsWith("steal")) {
            return new WorkStealingGrepper(charToInt(grepperType.charAt(5)), BoyerMooreHorspoolMatcher.FACTORY);
        }
        if (grepperType.startsWith("staged")) {
            return new StagedGrepper(charToInt(grepperType.charAt(6)), charToInt(grepperType.charAt(7)),
                            BoyerMooreHorspoolMatcher.FACTORY);
        }
        return null;
    }

    private static int charToInt(char ch) {
        return ch - 48;
    }

    public void timeGrepper(int reps) throws InterruptedException {
        for (int i = 0; i < reps; i++) {
            Grepper grepper = createGrepper(grepperType);
            BenchmarkUtil.timeGrepper(grepper);
        }
    }

    @Test
    public void runBenchmark() throws Exception {
        BenchmarkUtil.runBenchmark(MainBenchmark.class, 5);
    }

    @AfterClass
    public static void afterClass() throws IOException {
        BenchmarkUtil.clearTestData();
    }
}
