package org.parilin.dbgrep.benchmark;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.parilin.dbgrep.BoyerMooreHorspoolMatcher;
import org.parilin.dbgrep.WorkStealingGrepper;

import com.google.caliper.Param;
import com.google.caliper.SimpleBenchmark;

public class WorkStealingGrepperBenchmark extends SimpleBenchmark {

    @BeforeClass
    public static void beforeClass() throws IOException {
        BenchmarkUtil.prepareTestData();
    }

    @Param({"1", "2", "3", "4", "5", "6"})
    int threads;

    WorkStealingGrepper grepper;

    public void timeWorkStealingGrepper(int reps) throws InterruptedException {
        for (int i = 0; i < reps; i++) {
            BenchmarkUtil.timeGrepper(new WorkStealingGrepper(threads, BoyerMooreHorspoolMatcher.FACTORY));
        }
    }

    @Test
    public void runBenchmark() throws Exception {
        BenchmarkUtil.runBenchmark(WorkStealingGrepperBenchmark.class, 1);
    }

    @AfterClass
    public static void afterClass() throws IOException {
        BenchmarkUtil.clearTestData();
    }
}
