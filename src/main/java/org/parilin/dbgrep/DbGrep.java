package org.parilin.dbgrep;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class DbGrep {

    private static final String READ_TREADS_OPT_NAME = "rt";

    private static final String THREADS_PAR_NAME = "t";

    private static final String TYPE_PAR_NAME = "type";

    private static final String SEQUENTIAL_TYPE = "seq";

    private static final String PARALLEL_TYPE = "par";

    private static final String STAGED_TYPE = "st";

    private static final String WORK_STEALING_TYPE = "ws";

    private static final String TYPE_PARAM_DESCR = //
        "Finds string pattern in the files recursively.\n"
            + "format: dbgrep [OPTIONS] PATTERN FILE\n"
            + "OPTIONS:\n"
            + "--type type of the grepper. (Default : ws)\n"
            + "   Acceptable values: \n"
            + "       seq - sequential (single threaded ordinary execution)\n"
            + "       par - parallel (multyple sequential grep tasks executed in parallel by file)\n"
            + "       st  - staged (some threads only read data and some only match content)\n"
            + "       ws  - work stealing (concurrently executes match tasks which is read from files on demand) \n"
            + "-t number of threads (applicable for all grepper types except sequential. in staged grepper means number of matching threads)\n"
            + "-rt number of file read threads (applicable only for staged grepper)";

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            printHelp();
            return;
        }
        OptionParser parser = new OptionParser() {
            {
                accepts(TYPE_PAR_NAME).withRequiredArg().defaultsTo("ws");
                accepts(THREADS_PAR_NAME).withRequiredArg().ofType(Integer.class);
                accepts(READ_TREADS_OPT_NAME).withRequiredArg().ofType(Integer.class); // only for staged
            }
        };
        OptionSet opts = parser.parse(args);
        List<String> argsEnd = opts.nonOptionArguments();
        if (argsEnd.size() != 2) {
            System.out.println("Invalid number of arguments.");
            printHelp();
            return;
        }
        String needle = argsEnd.get(0);
        Path dir = FileSystems.getDefault().getPath(argsEnd.get(1));
        if (!Files.exists(dir)) {
            System.out.printf("Target directory %s doesn't exist.%n", dir);
            return;
        }
        Charset charset = Charset.defaultCharset();
        Grepper grepper = createGrepper(opts);
        try {
            grepper.grep(dir, needle.toCharArray(), charset, new SysoutResultsCollector());
        } catch (InterruptedException e) {
            return;
        }
    }

    private static Grepper createGrepper(OptionSet opts) {
        String type = "ws";
        if (opts.has(TYPE_PAR_NAME)) {
            type = (String) opts.valueOf(TYPE_PAR_NAME);
        }
        int threads;
        if (opts.has(THREADS_PAR_NAME)) {
            threads = (Integer) opts.valueOf(THREADS_PAR_NAME);
        } else {
            threads = Runtime.getRuntime().availableProcessors() + 1;
        }
        int readThread = -1;
        if (opts.has(READ_TREADS_OPT_NAME)) {
            readThread = (Integer) opts.valueOf(READ_TREADS_OPT_NAME);
        }
        switch (type) {
            case SEQUENTIAL_TYPE:
                return new SequentialGrepper(BoyerMooreHorspoolMatcher.FACTORY);
            case PARALLEL_TYPE:
                return new ParallelGrepper(threads, BoyerMooreHorspoolMatcher.FACTORY);
            case WORK_STEALING_TYPE:
                return new WorkStealingGrepper(threads, BoyerMooreHorspoolMatcher.FACTORY);
            case STAGED_TYPE:
                if (readThread == -1) {
                    readThread = threads / 2;
                    if (readThread == 0) {
                        readThread = 1;
                    } else {
                        threads -= readThread;
                    }
                }
                return new StagedGrepper(readThread, threads, BoyerMooreHorspoolMatcher.FACTORY);
            default:
                return new SequentialGrepper(BoyerMooreHorspoolMatcher.FACTORY);
        }
    }

    private static void printHelp() {
        System.out.println(TYPE_PARAM_DESCR);
    }

}
