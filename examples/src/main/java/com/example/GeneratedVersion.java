package com.example;

import java.util.Date;

public final class GeneratedVersion implements Comparable<GeneratedVersion> {
    public static final Date BUILD_DATE = new Date(1720371304587L);
    public static final int MAJOR = 1;
    public static final int MINOR = 0;
    public static final String PROJECT = "My App";
    public static final String QUALIFIER = "rc1";
    public static final int REVISION = 1;
    public static final String VERSION = "1.0.1-rc1";

    private GeneratedVersion() {
        // no-op
    }

    @Override
    public int compareTo(GeneratedVersion other) {
        if (MAJOR != other.MAJOR) {
            return Integer.compare(MAJOR, other.MAJOR);
        } else if (MINOR != other.MINOR) {
            return Integer.compare(MINOR, other.MINOR);
        } else if (REVISION != other.REVISION) {
            return Integer.compare(REVISION, other.REVISION);
        } else {
            return QUALIFIER.compareTo(other.QUALIFIER);
        }
    }
}