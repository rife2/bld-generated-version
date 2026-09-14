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
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import rife.bld.Project;
import rife.bld.dependencies.VersionNumber;
import rife.bld.extension.tools.IOTools;
import rife.bld.testing.BlankSource;
import rife.resources.ResourceFinderDirectories;
import rife.resources.ResourceFinderGroup;
import rife.template.Template;
import rife.template.TemplateFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

@SuppressWarnings({"PMD.AvoidDuplicateLiterals", "DataFlowIssue"})
class GeneratedVersionTest {

    @Nested
    @DisplayName("ClassName Tests")
    class ClassNameTests {

        @ParameterizedTest
        @ValueSource(strings = {"MyVersion", "_Valid", "$Valid", "A1"})
        @DisplayName("setClassName accepts valid identifiers")
        void setClassNameAcceptsValid(String name) {
            var gv = new GeneratedVersion();
            assertThatNoException().isThrownBy(() -> gv.setClassName(name));
            assertThat(gv.getClassName()).isEqualTo(name);
        }

        @ParameterizedTest
        @ValueSource(strings = {"123Bad", "com.bad", "My Class", "synchronized"})
        @DisplayName("setClassName rejects invalid identifiers")
        void setClassNameRejectsInvalid(String name) {
            var gv = new GeneratedVersion();
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> gv.setClassName(name))
                    .withMessageStartingWith("Invalid class name:");
        }

        @Test
        @DisplayName("setClassName rejects null")
        void setClassNameRejectsNull() {
            assertThatNullPointerException().isThrownBy(() -> new GeneratedVersion().setClassName(null));
        }
    }

    @Nested
    @DisplayName("Extension Tests")
    class ExtensionTests {

        @Test
        @DisplayName("default extension is .java")
        void defaultExtensionIsJava() {
            assertThat(new GeneratedVersion().getExtension()).isEqualTo(".java");
        }

        @Test
        @DisplayName("setExtension accepts value with leading dot and trims")
        void setExtensionAcceptsDotPrefix() {
            var gv = new GeneratedVersion();
            gv.setExtension("  .kt  ");
            assertThat(gv.getExtension()).isEqualTo(".kt");
        }

        @ParameterizedTest
        @BlankSource
        @DisplayName("setExtension rejects blank values")
        void setExtensionRejectsBlank(String ext) {
            var gv = new GeneratedVersion();
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> gv.setExtension(ext))
                    .withMessageContaining("extension must not be blank");
        }

        @ParameterizedTest
        @ValueSource(strings = {"java", "."})
        @DisplayName("setExtension rejects invalid values")
        void setExtensionRejectsInvalid(String ext) {
            var gv = new GeneratedVersion();
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> gv.setExtension(ext))
                    .withMessageContaining("Extension must be '.'");
        }

        @Test
        @DisplayName("setExtension rejects null")
        void setExtensionRejectsNull() {
            assertThatNullPointerException().isThrownBy(() -> new GeneratedVersion().setExtension(null));
        }
    }

    @Nested
    @DisplayName("FillTemplate Tests")
    class FillTemplateTests {

        @Test
        @DisplayName("fillTemplate blanks qualify")
        void fillTemplateBlanksQualifier() {
            var gv = new GeneratedVersion();
            gv.setProject(project(new VersionNumber(1, 0, 0))); // no qualifier

            var tpl = template();

            gv.fillTemplate(tpl);

            assertThat(tpl.getValue("qualifier")).isBlank();
        }

        @Test
        @DisplayName("fillTemplate requires template and project")
        void fillTemplateRequiresArgs() {
            var gv = new GeneratedVersion();
            assertThatNullPointerException().isThrownBy(() -> gv.fillTemplate(null));
            assertThatNullPointerException().isThrownBy(() -> gv.fillTemplate(template()));
        }

        @Test
        @DisplayName("fillTemplate resolves package/project from project when unset")
        void fillTemplateResolvesFromProject() {
            var gv = new GeneratedVersion();
            gv.setProject(project(new VersionNumber(2, 0, 0)));

            var tpl = template();
            tpl.setValue("packageName", "");
            tpl.setValue("project", "");

            gv.fillTemplate(tpl);

            assertThat(tpl.getValue("packageName")).isEqualTo("com.example");
            assertThat(tpl.getValue("project")).isEqualTo("test-app");
        }

        @Test
        @DisplayName("fillTemplate sets non-blank qualifier")
        void fillTemplateSetsQualifier() {
            var gv = new GeneratedVersion();
            gv.setProject(project(new VersionNumber(1, 0, 0, "SNAPSHOT")));

            var tpl = template();
            tpl.setValue("qualifier", "");

            gv.fillTemplate(tpl);

            assertThat(tpl.getValue("qualifier")).isEqualTo("SNAPSHOT");
        }

        @Test
        @DisplayName("fillTemplate sets all standard values")
        void fillTemplateSetsValues() {
            var gv = new GeneratedVersion();
            gv.setProject(project(new VersionNumber(1, 2, 3)));
            gv.setClassName("AppVersion");
            gv.setPackageName("com.test");

            var tpl = template();
            tpl.setValue("package", "");
            tpl.setValue("packageName", "");
            tpl.setValue("className", "");
            tpl.setValue("project", "");
            tpl.setValue("version", "");
            tpl.setValue("major", "");
            tpl.setValue("minor", "");
            tpl.setValue("revision", "");
            tpl.setValue("epoch", "");

            gv.fillTemplate(tpl);

            assertThat(tpl.getValue("package")).contains("package com.test;");
            assertThat(tpl.getValue("packageName")).isEqualTo("com.test");
            assertThat(tpl.getValue("className")).isEqualTo("AppVersion");
            assertThat(tpl.getValue("project")).isEqualTo("test-app");
            assertThat(tpl.getValue("version")).isEqualTo("1.2.3");
            assertThat(tpl.getValue("major")).isEqualTo("1");
            assertThat(tpl.getValue("minor")).isEqualTo("2");
            assertThat(tpl.getValue("revision")).isEqualTo("3");
            assertThat(tpl.getValue("epoch")).matches("\\d{13}"); // millis
        }

        @SuppressWarnings("PMD.CallSuperInConstructor")
        private Project project(VersionNumber versionNumber) {
            class TestProject extends Project {

                TestProject() {
                    name = "test-app";
                    pkg = "com.example";
                    version = versionNumber;
                }
            }
            return new TestProject();
        }

        private Template template() {
            var group = new ResourceFinderGroup().add(
                    new ResourceFinderDirectories(
                            IOTools.resolveFile(new File("src"), "test", "resources", "foo")));
            return TemplateFactory.TXT.setResourceFinder(group).get("version_test");
        }
    }

    @Nested
    @DisplayName("Null Safety Tests")
    class NullSafetyTests {

        @Test
        void setDirectoryRejectsNull() {
            assertThatNullPointerException().isThrownBy(() -> new GeneratedVersion().setDirectory(null));
        }

        @Test
        void setProjectNameRejectsNull() {
            assertThatNullPointerException().isThrownBy(() -> new GeneratedVersion().setProjectName(null));
        }

        @Test
        void setProjectRejectsNull() {
            assertThatNullPointerException().isThrownBy(() -> new GeneratedVersion().setProject(null));
        }

        @Test
        void setTemplateRejectsNull() {
            assertThatNullPointerException().isThrownBy(() -> new GeneratedVersion().setTemplate(null));
        }
    }

    @Nested
    @DisplayName("PackageName Tests")
    class PackageNameTests {

        @Test
        @DisplayName("setPackageName accepts empty string for default package")
        void setPackageNameAcceptsEmpty() {
            var gv = new GeneratedVersion();
            gv.setPackageName("");
            assertThat(gv.getPackageName()).isEmpty();
        }

        @ParameterizedTest
        @ValueSource(strings = {"123bad", "com.class", "com..bad"})
        @DisplayName("setPackageName rejects invalid package")
        void setPackageNameRejectsInvalid(String pkg) {
            var gv = new GeneratedVersion();
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> gv.setPackageName(pkg))
                    .withMessageStartingWith("Invalid package name:");
        }

        @Test
        @DisplayName("setPackageName rejects null")
        void setPackageNameRejectsNull() {
            assertThatNullPointerException().isThrownBy(() -> new GeneratedVersion().setPackageName(null));
        }
    }

    @Nested
    @DisplayName("WriteTemplate Tests")
    class WriteTemplateTests {

        @SuppressWarnings("PMD.CallSuperInConstructor")
        private Project project() {
            class TestProject extends Project {

                TestProject() {
                    name = "app";
                    pkg = "com.test";
                    version = new VersionNumber(1, 0, 0);
                }
            }
            return new TestProject();
        }

        private Template simpleTpl() {
            var group = new ResourceFinderGroup().add(
                    new ResourceFinderDirectories(
                            IOTools.resolveFile(new File("src"), "test", "resources")));
            return TemplateFactory.TXT.setResourceFinder(group).get("version_simple_test");
        }

        @Test
        @DisplayName("writeTemplate creates file with package path")
        void writeTemplateCreatesFile(@TempDir Path dir) throws IOException {
            var gv = new GeneratedVersion();
            gv.setProject(project());
            gv.setDirectory(dir.toFile());
            gv.setPackageName("com.test");
            gv.setClassName("Version");

            var tpl = simpleTpl();
            tpl.setValue("className", "Version");

            var created = gv.writeTemplate(tpl);

            assertThat(created).isPresent();
            assertThat(dir.resolve("com/test/Version.java")).exists().content()
                    .isEqualTo("public final class Version { }");
        }

        @Test
        @DisplayName("writeTemplate handles default package")
        void writeTemplateDefaultPackage(@TempDir Path dir) throws IOException {
            var gv = new GeneratedVersion();
            gv.setProject(project());
            gv.setDirectory(dir.toFile());
            gv.setPackageName("");
            gv.setClassName("Version");

            gv.writeTemplate(simpleTpl());

            assertThat(dir.resolve("Version.java")).exists();
        }

        @Test
        @DisplayName("writeTemplate overwrite=false skips existing file")
        void writeTemplateOverwriteFalse(@TempDir Path dir) throws IOException {
            var gv = new GeneratedVersion();
            gv.setProject(project());
            gv.setDirectory(dir.toFile());
            gv.setClassName("Version");

            Files.createDirectories(dir.resolve("com/test"));
            var existing = dir.resolve("com/test/Version.java");
            Files.writeString(existing, "OLD");

            var created = gv.writeTemplate(simpleTpl(), "Version.java", false);

            assertThat(created).isNotPresent();
            assertThat(existing).content().isEqualTo("OLD");
        }

        @Test
        @DisplayName("writeTemplate overwrite=true replaces existing file")
        void writeTemplateOverwriteTrue(@TempDir Path dir) throws IOException {
            var gv = new GeneratedVersion();
            gv.setProject(project());
            gv.setDirectory(dir.toFile());
            gv.setClassName("Version");

            var tpl = simpleTpl();
            tpl.setValue("className", "Version");

            gv.writeTemplate(tpl); // first write
            var second = gv.writeTemplate(tpl); // overwrite

            assertThat(second).isPresent();
            assertThat(dir.resolve("com/test/Version.java")).content()
                    .isEqualTo("public final class Version { }");
        }

        @Test
        @DisplayName("writeTemplate requires template, project, directory")
        void writeTemplateRequiresArgs() {
            var gv = new GeneratedVersion();
            assertThatNullPointerException().isThrownBy(() -> gv.writeTemplate(null));

            gv.setProject(project());
            assertThatNullPointerException().isThrownBy(() -> gv.writeTemplate(simpleTpl()));
        }
    }
}
