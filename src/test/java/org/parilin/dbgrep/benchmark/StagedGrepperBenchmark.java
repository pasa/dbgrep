package org.parilin.dbgrep.benchmark;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.parilin.dbgrep.BoyerMooreHorspoolMatcher;
import org.parilin.dbgrep.StagedGrepper;

import com.google.caliper.Param;
import com.google.caliper.SimpleBenchmark;

public class StagedGrepperBenchmark extends SimpleBenchmark {

    @BeforeClass
    public static void beforeClass() throws IOException {
        BenchmarkUtil.prepareTestData();
    }

    @Param({"1", "2", "3"})
    int readthreads;

    @Param({"1", "2", "3"})
    int matchThreads;

    StagedGrepper grepper;

    @Override
    public void setUp() {
        grepper = new StagedGrepper(readthreads, matchThreads, BoyerMooreHorspoolMatcher.FACTORY);
    }

    public void timeStagedGrepper(int reps) throws InterruptedException {
        for (int i = 0; i < reps; i++) {
            BenchmarkUtil.timeGrepper(grepper);
        }
    }

    @Test
    public void runBenchmark() throws Exception {
        BenchmarkUtil.runBenchmark(StagedGrepperBenchmark.class, 1);
    }

    @AfterClass
    public static void afterClass() throws IOException {
        BenchmarkUtil.clearTestData();
    }
}
