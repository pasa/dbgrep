package org.parilin.dbgrep;

import static org.junit.Assert.assertThat;
import static org.junit.internal.matchers.IsCollectionContaining.hasItems;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class DepthFirstFilesWalkerTest {

    @Test
    public void testNext() throws Exception {
        FileSystem fs = FileSystems.getDefault();
        Path start = fs.getPath("test_data", "walker");
        DepthFirstFilesWalker walker = new DepthFirstFilesWalker(start);
        List<Path> paths = new ArrayList<>();
        for (;;) {
            Path path = walker.next();
            if (path == null) {
                break;
            } else {
                paths.add(path);
            }
        }
        Path p1 = fs.getPath("test_data", "walker", "1");
        Path p2 = fs.getPath("test_data", "walker", "2", "21");
        Path p3 = fs.getPath("test_data", "walker", "3");
        assertThat(paths, hasItems(p1, p2, p3));
    }
}
