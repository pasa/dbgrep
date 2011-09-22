package org.parilin.dbgrep;

import java.nio.ByteBuffer;

public interface Matcher {

	void match(ByteBuffer source);
}
