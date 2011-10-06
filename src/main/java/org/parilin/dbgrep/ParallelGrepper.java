package org.parilin.dbgrep;

import java.nio.charset.Charset;
import java.nio.file.Path;

public class ParallelGrepper extends TaskableGrepper {

    static class ParallelTaskStrategy implements GrepperTaskStrategy {

        private ResultsMerger merger;

        private Charset charset;

        private ResultsCollector collector;

        @Override
        public void init(Path dir, char[] needle, Charset charset, ResultsCollector collector) {
            this.charset = charset;
            this.collector = collector;
            merger = new ConcurrentResultsMerger(needle);
        }

        @Override
        public Runnable createTask(FilesWalker walker, Matcher matcher, int bufferSize) {
            return new SequentialGrepperTask(matcher, walker, charset, collector, merger, bufferSize);
        }
    }

    public ParallelGrepper(int threads, MatcherFactory matcherFactory, int bufferSize) {
        super(new ParallelTaskStrategy(), threads, matcherFactory, bufferSize);
    }

}
