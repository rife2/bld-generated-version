package com.example;

import rife.bld.BuildCommand;
import rife.bld.Project;
import rife.bld.extension.GeneratedVersionOperation;

import java.util.List;

import static rife.bld.dependencies.Repository.MAVEN_CENTRAL;
import static rife.bld.dependencies.Repository.RIFE2_RELEASES;
import static rife.bld.dependencies.Scope.test;

/**
 * Example build.
 */
public class SampleBuild extends Project {
    public SampleBuild() {
        pkg = "com.example";
        name = "Sample";
        mainClass = "com.example.SampleMain";
        version = version(1, 0, 1, "rc1");

        javaRelease = 17;

        autoDownloadPurge = true;
        downloadSources = true;

        repositories = List.of(MAVEN_CENTRAL, RIFE2_RELEASES);
        scope(test)
                .include(dependency("org.junit.jupiter", "junit-jupiter", version(5, 12, 1)))
                .include(dependency("org.junit.platform", "junit-platform-console-standalone", version(1, 12, 1)));
    }

    public static void main(String[] args) {
        new SampleBuild().start(args);
    }

    @Override
    public void compile() throws Exception {
        genver();
        super.compile();
    }

    @BuildCommand(summary = "Generates version class")
    public void genver() throws Exception {
        new GeneratedVersionOperation()
                .fromProject(this)
//                .projectName("My App")
//                .classTemplate("my_app_version.txt")
//                .classTemplate("version.txt")
                .execute();
    }
}
