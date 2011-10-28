package org.parilin.dbgrep.benchmark;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

import org.parilin.dbgrep.Grepper;
import org.parilin.dbgrep.ResultsCollector;

import com.google.caliper.Benchmark;
import com.google.caliper.Runner;
import com.google.common.collect.ObjectArrays;

public final class BenchmarkUtil {

    static Path CLASSICS = FileSystems.getDefault().getPath("target", "classics");

    static char[] PATTERN = "однако".toCharArray();

    static ResultsCollector EMPTY_COLLECTOR = new EmptyCollector();

    static class EmptyCollector implements ResultsCollector {

        @Override
        public void matches(Path file, long[] matches) {
            // do nothing
        }

        @Override
        public void exception(Throwable t) {
            // do nothing
        }

    }

    public BenchmarkUtil() {
        throw new UnsupportedOperationException();
    }

    public static void timeGrepper(Grepper grepper) throws InterruptedException {
        grepper.grep(CLASSICS, PATTERN, Charset.forName("UTF-8"), EMPTY_COLLECTOR);
    }

    public static void runBenchmark(Class<? extends Benchmark> benchmark, int trials) {
        String[] args =
            ObjectArrays.concat(new String[] {"--trials", Integer.toString(trials)}, benchmark.getName());
        new Runner().run(args);
    }

    public static void debugBenchmark(Class<? extends Benchmark> benchmark) {
        String[] args = ObjectArrays.concat(new String[] {"--debug"}, benchmark.getName());
        new Runner().run(args);
    }

    private static FileSystem createZipFileSystem(String zipFilename, boolean create) throws IOException {
        // convert the filename to a URI
        final Path path = Paths.get(zipFilename);
        final URI uri = URI.create("jar:file:" + path.toUri().getPath());

        final Map<String, String> env = new HashMap<>();
        if (create) {
            env.put("create", "true");
        }
        return FileSystems.newFileSystem(uri, env);
    }

    public static void unzip(String zipFilename, String destDirname) throws IOException {

        final Path destDir = Paths.get(destDirname);
        // if the destination doesn't exist, create it
        if (Files.notExists(destDir)) {
            Files.createDirectories(destDir);
        }

        try (FileSystem zipFileSystem = createZipFileSystem(zipFilename, false)) {
            final Path root = zipFileSystem.getPath("/");

            Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    final Path destFile = Paths.get(destDir.toString(), file.toString());
                    Files.copy(file, destFile, StandardCopyOption.REPLACE_EXISTING);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    final Path dirToCreate = Paths.get(destDir.toString(), dir.toString());
                    if (Files.notExists(dirToCreate)) {
                        Files.createDirectory(dirToCreate);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    public static void prepareTestData() throws IOException {
        unzip("test_data/classics.zip", "target");
    }

    public static void clearTestData() throws IOException {
        Files.walkFileTree(CLASSICS, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
