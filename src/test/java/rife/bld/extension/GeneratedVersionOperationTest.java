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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import rife.bld.BaseProject;
import rife.bld.Project;
import rife.bld.blueprints.BaseProjectBlueprint;
import rife.bld.dependencies.VersionNumber;
import rife.bld.extension.testing.LoggingExtension;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

/**
 * Integration tests for {@link GeneratedVersionOperation}.
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @since 1.0
 */
@ExtendWith(LoggingExtension.class)
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class GeneratedVersionOperationTest {

    @RegisterExtension
    @SuppressWarnings({"unused"})
    private static final LoggingExtension loggingExtension =
            new LoggingExtension(GeneratedVersionOperation.class.getName());

    private final BaseProject baseProject = new Project() {
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

    @Nested
    @DisplayName("classTemplate Tests")
    class ClassTemplateTests {

        private final File templateFile = new File("src/test/resources/foo/version_test.txt");

        @Test
        @DisplayName("classTemplate(File) sets template on inner GeneratedVersion")
        void classTemplateAsFile() {
            var op = new GeneratedVersionOperation().classTemplate(templateFile);
            assertThat(op.generatedVersion().getTemplate()).isEqualTo(templateFile);
        }

        @Test
        @DisplayName("classTemplate(Path) sets template on inner GeneratedVersion")
        void classTemplateAsPath() {
            var op = new GeneratedVersionOperation().classTemplate(templateFile.toPath());
            assertThat(op.generatedVersion().getTemplate()).isEqualTo(templateFile);
        }

        @Test
        @DisplayName("classTemplate(String) sets template on inner GeneratedVersion")
        void classTemplateAsString() {
            var op = new GeneratedVersionOperation().classTemplate(templateFile.getPath());
            assertThat(op.generatedVersion().getTemplate()).isEqualTo(templateFile);
        }
    }

    @Nested
    @DisplayName("Directory Tests")
    class DirectoryTests {

        private final File bar = new File("bar");
        private final File foo = new File("foo");

        @Test
        @DisplayName("directory(File) sets directory on inner GeneratedVersion")
        void directoriesAsFile() {
            var op = new GeneratedVersionOperation().directory(foo);
            assertThat(op.generatedVersion().getDirectory()).isEqualTo(foo);
        }

        @Test
        @DisplayName("directory(Path) sets directory on inner GeneratedVersion")
        void directoriesAsPath() {
            var op = new GeneratedVersionOperation().directory(bar.toPath());
            assertThat(op.generatedVersion().getDirectory()).isEqualTo(bar);
        }

        @Test
        @DisplayName("directory(String) sets directory on inner GeneratedVersion")
        void directoriesAsString() {
            var op = new GeneratedVersionOperation().directory("foo");
            assertThat(op.generatedVersion().getDirectory()).isEqualTo(foo);
        }
    }

    @Nested
    @DisplayName("Execute Guard Tests")
    class ExecuteGuardTests {

        @Test
        @DisplayName("execute throws when directory is not set")
        void throwsWhenDirectoryIsNull() {
            var op = new GeneratedVersionOperation();
            op.generatedVersion().setProject(baseProject);
            assertThatNullPointerException().isThrownBy(op::execute);
        }

        @Test
        @DisplayName("execute throws when project is not set")
        void throwsWhenProjectIsNull() {
            var op = new GeneratedVersionOperation().directory(tmpDir);
            assertThatNullPointerException().isThrownBy(op::execute);
        }
    }

    @Nested
    @DisplayName("Execution Tests")
    class ExecutionTests {

        @Test
        @DisplayName("example template produces expected output file")
        void example() throws Exception {
            new GeneratedVersionOperation()
                    .fromProject(new BaseProjectBlueprint(new File("examples"), "com.example", "Example", "Example"))
                    .directory(tmpDir.getAbsolutePath())
                    .classTemplate(new File("examples", "version.txt"))
                    .execute();

            var template = Path.of(tmpDir.getAbsolutePath(), "com", "example", "GeneratedVersion.java");
            assertThat(template).exists();
        }

        @Test
        @DisplayName("example template output contains expected class declaration")
        void exampleOutputContainsClassDeclaration() throws Exception {
            new GeneratedVersionOperation()
                    .fromProject(new BaseProjectBlueprint(new File("examples"), "com.example", "Example", "Example"))
                    .directory(tmpDir.getAbsolutePath())
                    .classTemplate(new File("examples", "version.txt"))
                    .execute();

            var content = Files.readString(
                    Path.of(tmpDir.getAbsolutePath(), "com", "example", "GeneratedVersion.java"));
            assertThat(content).contains("class GeneratedVersion");
        }

        @Test
        @DisplayName("example template output does not contain default-template-only markers")
        void exampleOutputExcludesDefaultMarkers() throws Exception {
            new GeneratedVersionOperation()
                    .fromProject(new BaseProjectBlueprint(new File("examples"), "com.example", "Example", "Example"))
                    .directory(tmpDir.getAbsolutePath())
                    .classTemplate(new File("examples", "version.txt"))
                    .execute();

            var template = Path.of(tmpDir.getAbsolutePath(), "com", "example", "GeneratedVersion.java");
            assertThat(Files.readString(template)).doesNotContain("ERASED!");
        }

        @Test
        @DisplayName("execute output contains correct major version")
        void executeOutputContainsMajorVersion() throws Exception {
            new GeneratedVersionOperation()
                    .fromProject(baseProject)
                    .directory(tmpDir.getAbsolutePath())
                    .className("MyVersion")
                    .classTemplate("src/test/resources/foo/version_test.txt")
                    .packageName("")
                    .execute();

            var content = Files.readString(new File(tmpDir, "MyVersion.java").toPath());
            assertThat(content).contains("MAJOR = 2");
        }

        @Test
        @DisplayName("execute output contains correct project name")
        void executeOutputContainsProjectName() throws Exception {
            new GeneratedVersionOperation()
                    .fromProject(baseProject)
                    .directory(tmpDir.getAbsolutePath())
                    .className("MyVersion")
                    .classTemplate("src/test/resources/foo/version_test.txt")
                    .packageName("")
                    .execute();

            var content = Files.readString(new File(tmpDir, "MyVersion.java").toPath());
            assertThat(content).contains("PROJECT = \"MyExample\"");
        }

        @Test
        @DisplayName("execute output omits package declaration when packageName is empty")
        void executeOutputOmitsPackageWhenEmpty() throws Exception {
            new GeneratedVersionOperation()
                    .fromProject(baseProject)
                    .directory(tmpDir.getAbsolutePath())
                    .className("MyVersion")
                    .classTemplate("src/test/resources/foo/version_test.txt")
                    .packageName("")
                    .execute();

            var content = Files.readString(new File(tmpDir, "MyVersion.java").toPath());
            assertThat(content).doesNotContain("package");
        }

        @Test
        @DisplayName("execute produces class file at expected location")
        void executeProducesClassFile() throws Exception {
            new GeneratedVersionOperation()
                    .fromProject(baseProject)
                    .directory(tmpDir.getAbsolutePath())
                    .className("MyVersion")
                    .classTemplate("src/test/resources/foo/version_test.txt")
                    .packageName("")
                    .execute();

            assertThat(new File(tmpDir, "MyVersion.java")).exists();
        }
    }

    @Nested
    @DisplayName("Fluent API Tests")
    class FluentApiTests {

        @Test
        @DisplayName("className sets class name on inner GeneratedVersion")
        void classNameSetsValue() {
            var op = new GeneratedVersionOperation().fromProject(baseProject).className("MyVersion");
            assertThat(op.generatedVersion().getClassName()).isEqualTo("MyVersion");
        }

        @Test
        @DisplayName("extension sets extension on inner GeneratedVersion")
        void extensionSetsValue() {
            var op = new GeneratedVersionOperation().fromProject(baseProject).extension(".kt");
            assertThat(op.generatedVersion().getExtension()).isEqualTo(".kt");
        }

        @Test
        @DisplayName("generatedVersion() returns the mutable inner instance")
        void generatedVersionReturnsInnerInstance() {
            var op = new GeneratedVersionOperation();
            assertThat(op.generatedVersion()).isNotNull();
        }

        @Test
        @DisplayName("packageName sets package on inner GeneratedVersion")
        void packageNameSetsValue() {
            var op = new GeneratedVersionOperation().fromProject(baseProject).packageName("com.custom");
            assertThat(op.generatedVersion().getPackageName()).isEqualTo("com.custom");
        }

        @Test
        @DisplayName("projectName sets project name on inner GeneratedVersion")
        void projectNameSetsValue() {
            var op = new GeneratedVersionOperation().fromProject(baseProject).projectName("Custom App");
            assertThat(op.generatedVersion().getProjectName()).isEqualTo("Custom App");
        }
    }

    @Nested
    @DisplayName("fromProject Tests")
    class FromProjectTests {

        @Test
        @DisplayName("fromProject sets directory to srcMainJavaDirectory")
        void fromProjectSetsDirectory() {
            var op = new GeneratedVersionOperation().fromProject(baseProject);
            assertThat(op.generatedVersion().getDirectory()).isEqualTo(baseProject.srcMainJavaDirectory());
        }

        @Test
        @DisplayName("fromProject sets the project")
        void fromProjectSetsProject() {
            var op = new GeneratedVersionOperation().fromProject(baseProject);
            assertThat(op.generatedVersion().getProject()).isEqualTo(baseProject);
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    @SuppressWarnings("DataFlowIssue")
    class ValidationTests {

        @Test
        void classNameWithNullOrEmpty() {
            assertThatThrownBy(() -> new GeneratedVersionOperation().className(null))
                    .as("className null").isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> new GeneratedVersionOperation().className(""))
                    .as("className empty").isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> new GeneratedVersionOperation().className("   "))
                    .as("className blank").isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> new GeneratedVersionOperation().className("1Invalid"))
                    .as("className invalid").isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void classTemplateWithNullOrEmpty() {
            assertThatThrownBy(() -> new GeneratedVersionOperation().classTemplate((File) null))
                    .as("template file null").isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> new GeneratedVersionOperation().classTemplate((Path) null))
                    .as("template path null").isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> new GeneratedVersionOperation().classTemplate(""))
                    .as("template string empty").isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void directoryWithNull() {
            assertThatThrownBy(() -> new GeneratedVersionOperation().directory((File) null))
                    .as("directory file null").isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> new GeneratedVersionOperation().directory((Path) null))
                    .as("directory path null").isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> new GeneratedVersionOperation().directory((String) null))
                    .as("directory string null").isInstanceOf(NullPointerException.class);
        }

        @Test
        void executeRequiresProject() {
            var op = new GeneratedVersionOperation()
                    .directory(new File("build"))
                    .className("Version");
            assertThatThrownBy(op::execute)
                    .as("missing project").isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("project");
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "java", ".", "java ", ". "})
        void extensionWithInvalid(String ext) {
            assertThatThrownBy(() -> new GeneratedVersionOperation().extension(ext))
                    .as("invalid extension: " + ext).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void extensionWithNull() {
            assertThatThrownBy(() -> new GeneratedVersionOperation().extension(null))
                    .as("extension null").isInstanceOf(NullPointerException.class);
        }

        @Test
        void fromProjectWithNull() {
            assertThatThrownBy(() -> new GeneratedVersionOperation().fromProject(null))
                    .as("project null").isInstanceOf(NullPointerException.class);
        }

        @Test
        void packageNameWithNull() {
            assertThatThrownBy(() -> new GeneratedVersionOperation().packageName(null))
                    .as("packageName null").isInstanceOf(NullPointerException.class);
        }

        @Test
        void projectNameWithNull() {
            assertThatThrownBy(() -> new GeneratedVersionOperation().projectName(null))
                    .as("projectName null").isInstanceOf(NullPointerException.class);
        }
    }
}