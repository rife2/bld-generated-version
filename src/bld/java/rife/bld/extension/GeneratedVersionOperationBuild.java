package rife.bld.extension;

import rife.bld.BuildCommand;
import rife.bld.Project;
import rife.bld.publish.PublishDeveloper;
import rife.bld.publish.PublishLicense;
import rife.bld.publish.PublishScm;

import java.util.List;

import static rife.bld.dependencies.Repository.*;
import static rife.bld.dependencies.Scope.compile;
import static rife.bld.dependencies.Scope.test;
import static rife.bld.operations.JavadocOptions.DocLinkOption.NO_MISSING;
import static rife.bld.operations.TemplateType.TXT;

public class GeneratedVersionOperationBuild extends Project {
    public GeneratedVersionOperationBuild() {
        pkg = "rife.bld.extension";
        name = "GeneratedVersionOperation";
        version = version(0, 9, 1, "SNAPSHOT");

        javaRelease = 17;
        downloadSources = true;
        autoDownloadPurge = true;
        repositories = List.of(MAVEN_CENTRAL, RIFE2_RELEASES);

        var rife2 = version(1,7,0);
        scope(compile)
                .include(dependency("com.uwyn.rife2", "rife2", rife2))
                .include(dependency("com.uwyn.rife2", "bld", rife2));
        scope(test)
                .include(dependency("org.junit.jupiter", "junit-jupiter", version(5, 9, 3)))
                .include(dependency("org.junit.platform", "junit-platform-console-standalone", version(1, 9, 3)))
                .include(dependency("org.assertj:assertj-joda-time:2.2.0"));

        precompileOperation()
                .templateTypes(TXT);

        javadocOperation()
                .javadocOptions()
                .docLint(NO_MISSING)
                .link("https://rife2.github.io/rife2/")
                .link("https://javadoc.io/doc/net.sourceforge.pmd/pmd-core/latest/");

        publishOperation()
                .repositories(MAVEN_LOCAL, version.isSnapshot() ? repository("rife2-snapshot") : repository("rife2"))
                .info()
                .groupId("com.uwyn.rife2")
                .artifactId("bld-generated-version")
                .description("bld Extension to Generate Project Version Data")
                .url("https://github.com/rife2/generated-version")
                .developer(new PublishDeveloper().id("ethauvin").name("Erik C. Thauvin").email("erik@thauvin.net")
                        .url("https://erik.thauvin.net/"))
                .license(new PublishLicense().name("The Apache License, Version 2.0")
                        .url("http://www.apache.org/licenses/LICENSE-2.0.txt"))
                .scm(new PublishScm().connection("scm:git:https://github.com/rife2/generated-version.git")
                        .developerConnection("scm:git:git@github.com:rife2/generated-version.git")
                        .url("https://github.com/rife2/generated-version"))
                .signKey(property("sign.key"))
                .signPassphrase(property("sign.passphrase"));
    }

    public static void main(String[] args) {
        new GeneratedVersionOperationBuild().start(args);
    }

    @BuildCommand(summary = "Runs PMD analysis")
    public void pmd() throws Exception {
        new PmdOperation()
                .fromProject(this)
                .failOnViolation(true)
                .ruleSets("config/pmd.xml")
                .execute();
    }
}