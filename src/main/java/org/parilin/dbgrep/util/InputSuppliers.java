package org.parilin.dbgrep.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;

/**
 * Utils for input suppliers.
 */
public final class InputSuppliers {

    private InputSuppliers() {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates new file channel supplier.
     *
     * @param path path to file
     * @param options options
     * @return new file channel supplier
     */
    public static InputSupplier<FileChannel> newFileChannelSupplier(final Path path, final OpenOption... options) {
        return new InputSupplier<FileChannel>() {

            @Override
            public FileChannel getInput() throws IOException {
                return FileChannel.open(path, options);
            }
        };
    }

    public static InputSupplier<ReadableByteChannel> newArrayChannelSupplier(final byte[] bytes) {
        return new InputSupplier<ReadableByteChannel>() {

            @Override
            public ReadableByteChannel getInput() throws IOException {
                return Channels.newChannel(new ByteArrayInputStream(bytes));
            }
        };
    }
}
