package org.parilin.dbgrep;

import static org.parilin.dbgrep.util.InputSuppliers.newArrayChannelSupplier;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;

import org.junit.Assert;
import org.junit.Test;
import org.parilin.dbgrep.util.InputSupplier;

public class ChannelReaderTest {

    @Test
    public void testReadWithSmalBuffer() throws Exception {
        ByteBuffer buffer = ByteBuffer.allocate(10);
        Charset charset = Charset.forName("UTF-8");
        String text = "вдруг из маминой из спальни OLOLO пышь пышь реальни";
        byte[] bytes = text.getBytes(charset);
        CharBuffer cb = CharBuffer.allocate(7);
        StringBuilder sb = new StringBuilder();
        InputSupplier<ReadableByteChannel> in = newArrayChannelSupplier(bytes);
        try (ChannelReader reader = new ChannelReader(in, charset, buffer)) {
            while (reader.read(cb)) {
                cb.flip();
                sb.append(cb);
                cb.clear();
            }
        }
        Assert.assertEquals(text, sb.toString());
    }

}
