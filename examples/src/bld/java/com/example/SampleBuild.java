package com.example;

import rife.bld.BuildCommand;
import rife.bld.Project;
import rife.bld.extension.GeneratedVersionOperation;

import java.io.File;
import java.util.List;

import static rife.bld.dependencies.Repository.*;
import static rife.bld.dependencies.Scope.test;

/**
 * Example build.
 */
public class SampleBuild extends Project {

    final File generatedDirectory = new File(srcDirectory(), "generated");

    public SampleBuild() {
        pkg = "com.example";
        name = "Sample";
        mainClass = "com.example.SampleMain";
        version = version(1, 0, 1, "rc1");

        javaRelease = 17;

        autoDownloadPurge = true;
        downloadSources = true;

        repositories = List.of(MAVEN_CENTRAL, RIFE2_RELEASES, RIFE2_SNAPSHOTS);

        var junit = version(6, 1, 0);
        scope(test)
                .include(dependency("org.junit.jupiter", "junit-jupiter", junit))
                .include(dependency("org.junit.platform", "junit-platform-console-standalone", junit));

        compileOperation().mainSourceDirectories(generatedDirectory);
    }

    @Override
    public void compile() throws Exception {
        genver();
        super.compile();
    }

    public static void main(String[] args) {
        new SampleBuild().start(args);
    }

    @BuildCommand(summary = "Generates version class")
    public void genver() throws Exception {
        new GeneratedVersionOperation()
                .fromProject(this)
                .directory(generatedDirectory)
//                .projectName("My App")
//                .classTemplate("my_app_version.txt")
//                .classTemplate("version.txt")
//                .generateAnnotation(true)
                .execute();
    }
}
