package org.parilin.dbgrep;

import static java.nio.file.Files.exists;
import static java.nio.file.Files.newDirectoryStream;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * Depth first walker.
 */
@NotThreadSafe
public class DepthFirstFilesWalker implements FilesWalker {

    private final Path startPath;

    private boolean finished = false;

    private Iterator<Path> currIter;

    private DirectoryStream<Path> currDir;

    private Deque<Iterator<Path>> iterStack = new LinkedList<>();

    private Deque<DirectoryStream<Path>> dirStack = new LinkedList<>();

    public DepthFirstFilesWalker(Path startPath) {
        this.startPath = Objects.requireNonNull(startPath);
    }

    @Override
    public Path next() {
        if (finished) {
            return null;
        }
        if (currIter == null) { // init process
            if (!exists(startPath)) {
                return finished(null);
            }
            try {
                BasicFileAttributes attrs = attrs(startPath);
                if (attrs.isDirectory()) {
                    pushDir(newDirectoryStream(startPath));
                } else if (attrs.isRegularFile()) {
                    return finished(startPath);
                } else {
                    return finished(null);
                }
            } catch (IOException e) {
                return finished(null);
            }
        }
        for (;;) {
            while (!currIter.hasNext() && popDir()) {
                // rewind depth stack
            }
            if (currIter == null) { // stack is rewinded.
                return finished(null); // iteration finished
            }
            Path next = currIter.next();
            try {
                BasicFileAttributes attrs = attrs(next);
                if (attrs.isRegularFile()) {
                    return next;
                }
                if (attrs.isDirectory()) {
                    pushDir(newDirectoryStream(next));
                }
            } catch (IOException e) {
                // ignore
            }
        }
    }

    private void pushDir(DirectoryStream<Path> dir) {
        if (currDir != null) {
            dirStack.push(currDir);
            iterStack.push(currIter);
        }
        currDir = dir;
        currIter = dir.iterator();
    }

    private boolean popDir() {
        if (currDir != null) {
            try {
                currDir.close();
            } catch (IOException e) {
                // ignore
            }
        }
        if (dirStack.isEmpty()) {
            currDir = null;
            currIter = null;
            return false;
        } else {
            currDir = dirStack.pop();
            currIter = iterStack.pop();
            return true;
        }
    }

    private Path finished(Path ret) {
        finished = true;
        return ret;
    }

    private BasicFileAttributes attrs(Path path) throws IOException {
        return Files.getFileAttributeView(path, BasicFileAttributeView.class).readAttributes();
    }

    @Override
    public void close() throws IOException {
        finished = true;
        IOException lastException = null;
        while (popDir()) {
            try {
                currDir.close();
            } catch (IOException e) {
                lastException = e;
            }
        }
        if (lastException != null) {
            throw lastException;
        }
    }
}
