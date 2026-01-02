/*
 * Copyright 2023-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package rife.bld.extension;

import org.assertj.core.api.AutoCloseableSoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import rife.bld.BaseProject;
import rife.bld.Project;
import rife.bld.blueprints.BaseProjectBlueprint;
import rife.bld.dependencies.VersionNumber;
import rife.bld.extension.testing.LoggingExtension;
import rife.tools.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Implements the GeneratedVersionTests class.
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @since 1.0
 */
@ExtendWith(LoggingExtension.class)
class GeneratedVersionTests {
    @RegisterExtension
    @SuppressWarnings({"unused"})
    private static final LoggingExtension LOGGING_EXTENSION =
            new LoggingExtension(GeneratedVersionOperation.class.getName());

    private final BaseProject PROJECT = new Project() {
        @Override
        public String pkg() {
            return "com.example";
        }

        @Override
        public String name() {
            return "MyExample";
        }

        @Override
        public VersionNumber version() {
            return new VersionNumber(2, 1, 3);
        }
    };
    @TempDir
    private File tmpDir;

    /**
     * Compares two strings by removing all line separators and whitespace.
     *
     * @param text1 The first text to compare
     * @param text2 The second text to compare
     * @return true if the texts are equivalent when line separators are ignored, false otherwise
     */
    static boolean compareTextIgnoringLineSeparators(String text1, String text2) {
        // Handle null cases
        if (text1 == null && text2 == null) {
            return true;
        }
        if (text1 == null || text2 == null) {
            return false;
        }

        // Remove all line separators and whitespace
        var cleanedText1 = text1.replaceAll("\\r?\\n|\\r|\\s", "");
        var cleanedText2 = text2.replaceAll("\\r?\\n|\\r|\\s", "");

        // Compare the cleaned strings
        return cleanedText1.equals(cleanedText2);
    }

    @Test
    void generatedVersion() {
        var gv = new GeneratedVersion();
        gv.setProject(PROJECT);
        gv.setTemplate(new File(gv.getProject().srcTestResourcesDirectory().getAbsolutePath(), "version_test.txt"));
        gv.setPackageName("com.example.cool");
        gv.setProjectName("Cool App");
        gv.setClassName("CoolVersion");
        gv.setDirectory(new File("build"));
        gv.setExtension(".java");

        try (var softly = new AutoCloseableSoftAssertions()) {
            softly.assertThat(gv.getProject()).as("project").isEqualTo(PROJECT);
            softly.assertThat(gv.getTemplate()).as("template").exists();
            softly.assertThat(gv.getPackageName()).as("package name").isEqualTo("com.example.cool");
            softly.assertThat(gv.getProjectName()).as("project name").isEqualTo("Cool App");
            softly.assertThat(gv.getClassName()).as("class name").isEqualTo("CoolVersion");
            softly.assertThat(gv.getExtension()).as("extension").isEqualTo(".java");
            softly.assertThat(gv.getDirectory()).as("directory").isDirectory();
        }
    }

    @Nested
    @DisplayName("Directory Tests")
    class DirectoryTests {
        private final File bar = new File("bar");
        private final File foo = new File("foo");
        private final GeneratedVersionOperation op = new GeneratedVersionOperation();

        @Test
        void directoriesAsFile() {
            op.directory(foo);
            assertThat(op.generatedVersion().getDirectory()).as("as file").isEqualTo(foo);
        }

        @Test
        void directoriesAsPath() {
            var op = new GeneratedVersionOperation().directory(bar.toPath());
            assertThat(op.generatedVersion().getDirectory()).as("as path").isEqualTo(bar);
        }

        @Test
        void directoriesAsString() {
            op.directory("foo");
            assertThat(op.generatedVersion().getDirectory()).as("as string").isEqualTo(foo);
        }
    }

    @Nested
    @DisplayName("Execution Tests")
    class ExecutionTests {
        @Test
        void example() throws Exception {
            new GeneratedVersionOperation()
                    .fromProject(new BaseProjectBlueprint(new File("examples"), "com.example", "Example", "Example"))
                    .directory(tmpDir.getAbsolutePath())
                    //.classTemplate(new File("examples", "my_app_version.txt"))
                    .classTemplate(new File("examples", "version.txt"))
                    .execute();

            var template = Path.of(tmpDir.getAbsolutePath(), "com", "example", "GeneratedVersion.java");
            assertThat(template).exists();

            var content = Files.readString(template);
            assertThat(content).contains("class GeneratedVersion").contains("PROJECT = \"Example\";")
                    .contains("MAJOR = 0").contains("MINOR = 0").contains("REVISION = 1").contains("QUALIFIER = \"\"")
                    .doesNotContain("ERASED!"); // only in the default template
        }

        @Test
        void execute() throws Exception {
            new GeneratedVersionOperation()
                    .fromProject(PROJECT)
                    .directory(tmpDir.getAbsolutePath())
                    .extension(".java")
                    .classTemplate("src/test/resources/foo/version_test.txt")
                    .packageName("")
                    .className("MyVersion")
                    .execute();

            var template = new File(tmpDir, "MyVersion.java");
            assertThat(template).exists();

            var content = Files.readString(template.toPath());
            assertThat(content).contains("class MyVersion")
                    .contains("PROJECT = \"MyExample\";").contains("MAJOR = 2").contains("MINOR = 1")
                    .contains("REVISION = 3").contains("QUALIFIER = \"\"").contains("private MyVersion")
                    .doesNotContain("package");
        }
    }

    @Nested
    @DisplayName("Template Tests")
    class TemplateTests {
        @Test
        void buildCustomTemplate() {
            var gv = new GeneratedVersion();
            gv.setProject(PROJECT);
            gv.setTemplate(new File(gv.getProject().srcTestResourcesDirectory().getAbsolutePath(), "version_test.txt"));
            gv.setPackageName("com.example.my");
            gv.setProjectName("My App");
            gv.setClassName("MyVersion");

            var t = gv.buildTemplate();
            var v = gv.getProject().version();
            assertThat(v).extracting("majorInt", "minorInt", "revisionInt").as("version")
                    .containsExactly(2, 1, 3);
            assertThat(compareTextIgnoringLineSeparators(t.getContent(),
                    String.format("package %s;" +
                                    "public final class %s {" +
                                    "    public static final int PROJECT = \"%s\";" +
                                    "    public static final int MAJOR = %d;" +
                                    "    public static final int MINOR = %d;" +
                                    "    public static final int REVISION = %d;" +
                                    "    public static final String QUALIFIER = \"\";" +
                                    "    private MyVersion() {" +
                                    "        // no-op" +
                                    "    }" +
                                    "}", gv.getPackageName(), gv.getClassName(), gv.getProjectName(), v.majorInt(),
                            v.minorInt(), v.revisionInt()))).as("template").isTrue();
        }

        @Test
        void buildTemplate() {
            var gv = new GeneratedVersion();
            gv.setProject(PROJECT);

            var t = gv.buildTemplate();
            assertThat(t).isNotNull();

            try (var softly = new AutoCloseableSoftAssertions()) {
                softly.assertThat(gv.getProject()).isEqualTo(PROJECT);
                softly.assertThat(gv.getPackageName()).isEqualTo(PROJECT.pkg());
                softly.assertThat(gv.getProjectName()).isEqualTo(PROJECT.name());
            }

            assertThat(t.getContent()).contains("package com.example;").contains("class GeneratedVersion")
                    .contains("PROJECT = \"MyExample\";").contains("MAJOR = 2").contains("MINOR = 1")
                    .contains("REVISION = 3").contains("QUALIFIER = \"\"").contains("VERSION = \"2.1.3\"")
                    .contains("private GeneratedVersion");
        }

        @Test
        void writeTemplate() throws IOException {
            var gv = new GeneratedVersion();
            gv.setProject(PROJECT);
            gv.setDirectory(tmpDir);

            var t = gv.buildTemplate();
            gv.writeTemplate(t);

            assertThat(gv.getClassFile()).exists();

            var versionClass = FileUtils.readString(gv.getClassFile());
            assertThat(versionClass).contains("package com.example;").contains("class GeneratedVersion")
                    .contains("MAJOR = 2").contains("MINOR = 1").contains("REVISION = 3")
                    .contains("private GeneratedVersion");
        }
    }
}
