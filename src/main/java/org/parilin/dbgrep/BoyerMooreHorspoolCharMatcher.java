package org.parilin.dbgrep;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

import javax.annotation.concurrent.Immutable;

@Immutable
public class BoyerMooreHorspoolCharMatcher implements Matcher {

    private final char[] pattern;

    public BoyerMooreHorspoolCharMatcher(char[] pattern) {
	this.pattern = pattern;
    }

    public void match(ByteBuffer source) {
	CharBuffer asCharBuffer = source.asCharBuffer();
    }

}
