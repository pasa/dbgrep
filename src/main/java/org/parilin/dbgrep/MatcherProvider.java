package org.parilin.dbgrep;

public interface MatcherProvider {

    Matcher provide(char[] needle);
}
