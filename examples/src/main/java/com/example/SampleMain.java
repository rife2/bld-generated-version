package com.example;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class SampleMain {
    public static void main(String[] args) {
        final SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy 'at' HH:mm:ss z", Locale.US);

        System.out.println("-----------------------------------------------------");

        System.out.println("  Version: " + GeneratedVersion.PROJECT + ' ' + GeneratedVersion.VERSION);

        System.out.println("    Built on:       " + sdf.format(GeneratedVersion.BUILD_DATE));
        System.out.println("    Major:          " + GeneratedVersion.MAJOR);
        System.out.println("    Minor:          " + GeneratedVersion.MINOR);
        System.out.println("    Revision:       " + GeneratedVersion.REVISION);
        System.out.println("    Qualifier:      " + GeneratedVersion.QUALIFIER);

        System.out.println("-----------------------------------------------------");
    }
}