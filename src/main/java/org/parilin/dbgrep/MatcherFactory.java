package org.parilin.dbgrep;

public interface MatcherFactory {

    Matcher create(char[] needle);
}
