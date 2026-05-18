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
import rife.bld.Project;
import rife.bld.dependencies.VersionNumber;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link GeneratedVersion} in isolation.
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @since 1.0
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class GeneratedVersionTest {

    private final Project project = new Project() {
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

    @Nested
    @DisplayName("buildTemplate Tests")
    class BuildTemplateTests {

        @Test
        @DisplayName("buildTemplate does not mutate packageName")
        void buildTemplateDoesNotMutatePackageName() {
            var gv = new GeneratedVersion();
            gv.setProject(project);
            gv.setPackageName("com.custom");
            gv.buildTemplate();
            assertThat(gv.getPackageName()).isEqualTo("com.custom");
        }

        @Test
        @DisplayName("buildTemplate does not mutate projectName")
        void buildTemplateDoesNotMutateProjectName() {
            var gv = new GeneratedVersion();
            gv.setProject(project);
            gv.setProjectName("Custom App");
            gv.buildTemplate();
            assertThat(gv.getProjectName()).isEqualTo("Custom App");
        }

        @Test
        @DisplayName("buildTemplate leaves packageName null when not set")
        void buildTemplateLeavesPackageNameNull() {
            var gv = new GeneratedVersion();
            gv.setProject(project);
            gv.buildTemplate();
            assertThat(gv.getPackageName()).isNull();
        }

        @Test
        @DisplayName("buildTemplate leaves projectName null when not set")
        void buildTemplateLeavesProjectNameNull() {
            var gv = new GeneratedVersion();
            gv.setProject(project);
            gv.buildTemplate();
            assertThat(gv.getProjectName()).isNull();
        }

        @Test
        @DisplayName("returns non-null template when project is set")
        void returnsTemplate() {
            var gv = new GeneratedVersion();
            gv.setProject(project);
            assertThat(gv.buildTemplate()).isNotNull();
        }

        @Test
        @DisplayName("template content includes project name")
        void templateContentContainsProjectName() {
            var gv = new GeneratedVersion();
            gv.setProject(project);
            var t = gv.buildTemplate();
            assertThat(t.getContent()).contains("PROJECT = \"MyExample\"");
        }

        @Test
        @DisplayName("template content includes correct version parts")
        void templateContentContainsVersionParts() {
            var gv = new GeneratedVersion();
            gv.setProject(project);
            var t = gv.buildTemplate();
            assertThat(t.getContent()).contains("MAJOR = 2").contains("MINOR = 1").contains("REVISION = 3");
        }

        @Test
        @DisplayName("template content uses explicit packageName when set")
        void templateContentUsesExplicitPackageName() {
            var gv = new GeneratedVersion();
            gv.setProject(project);
            gv.setPackageName("org.custom");
            var t = gv.buildTemplate();
            assertThat(t.getContent()).contains("package org.custom");
        }

        @Test
        @DisplayName("template content includes explicit projectName when set")
        void templateContentUsesExplicitProjectName() {
            var gv = new GeneratedVersion();
            gv.setProject(project);
            gv.setProjectName("CustomApp");
            var t = gv.buildTemplate();
            assertThat(t.getContent()).contains("PROJECT = \"CustomApp\"");
        }

        @Test
        @DisplayName("template content uses project package when packageName not set")
        void templateContentUsesProjectPackage() {
            var gv = new GeneratedVersion();
            gv.setProject(project);
            var t = gv.buildTemplate();
            assertThat(t.getContent()).contains("package com.example");
        }

        @Test
        @DisplayName("throws NPE when project is not set")
        void throwsWhenProjectIsNull() {
            var gv = new GeneratedVersion();
            assertThatNullPointerException().isThrownBy(gv::buildTemplate);
        }
    }

    @Nested
    @DisplayName("Extension Tests")
    class ExtensionTests {

        @Test
        @DisplayName("default extension is .java")
        void defaultExtensionIsJava() {
            var gv = new GeneratedVersion();
            assertThat(gv.getExtension()).isEqualTo(".java");
        }

        @Test
        @DisplayName("setExtension accepts value with leading dot")
        void setExtensionAcceptsDotPrefix() {
            var gv = new GeneratedVersion();
            gv.setExtension(".kt");
            assertThat(gv.getExtension()).isEqualTo(".kt");
        }

        @Test
        @DisplayName("setExtension rejects value without leading dot")
        void setExtensionRejectsMissingDot() {
            var gv = new GeneratedVersion();
            assertThatIllegalArgumentException().isThrownBy(() -> gv.setExtension("java"));
        }
    }

    @SuppressWarnings("DataFlowIssue")
    @Nested
    @DisplayName("Null Safety Tests")
    class NullSafetyTests {

        @Test
        @DisplayName("setClassName accepts valid identifiers")
        void setClassNameAcceptsValid() {
            var gv = new GeneratedVersion();
            assertThatNoException().isThrownBy(() -> gv.setClassName("MyVersion"));
            assertThatNoException().isThrownBy(() -> gv.setClassName("_Valid"));
            assertThatNoException().isThrownBy(() -> gv.setClassName("$Valid"));
            assertThat(gv.getClassName()).isEqualTo("$Valid");
        }

        @Test
        @DisplayName("setClassName rejects invalid identifiers")
        void setClassNameRejectsInvalid() {
            var gv = new GeneratedVersion();
            assertThatIllegalArgumentException().isThrownBy(() -> gv.setClassName("123Bad"));
            assertThatIllegalArgumentException().isThrownBy(() -> gv.setClassName("com.bad"));
            assertThatIllegalArgumentException().isThrownBy(() -> gv.setClassName("My Class"));
            assertThatIllegalArgumentException().isThrownBy(() -> gv.setClassName("synchronized"));
            assertThatIllegalArgumentException().isThrownBy(() -> gv.setClassName(""));
            assertThatIllegalArgumentException().isThrownBy(() -> gv.setClassName(" "));
        }

        @Test
        @DisplayName("setClassName rejects null")
        void setClassNameRejectsNull() {
            var gv = new GeneratedVersion();
            assertThatNullPointerException().isThrownBy(() -> gv.setClassName(null));
        }

        @Test
        @DisplayName("setDirectory rejects null")
        void setDirectoryRejectsNull() {
            var gv = new GeneratedVersion();
            assertThatNullPointerException().isThrownBy(() -> gv.setDirectory(null));
        }

        @Test
        @DisplayName("setExtension rejects null")
        void setExtensionRejectsNull() {
            var gv = new GeneratedVersion();
            assertThatNullPointerException().isThrownBy(() -> gv.setExtension(null));
        }

        @Test
        @DisplayName("setPackageName rejects null")
        void setPackageNameRejectsNull() {
            var gv = new GeneratedVersion();
            assertThatNullPointerException().isThrownBy(() -> gv.setPackageName(null));
        }

        @Test
        @DisplayName("setProjectName rejects null")
        void setProjectNameRejectsNull() {
            var gv = new GeneratedVersion();
            assertThatNullPointerException().isThrownBy(() -> gv.setProjectName(null));
        }

        @Test
        @DisplayName("setProject rejects null")
        void setProjectRejectsNull() {
            var gv = new GeneratedVersion();
            assertThatNullPointerException().isThrownBy(() -> gv.setProject(null));
        }

        @Test
        @DisplayName("setTemplate rejects null")
        void setTemplateRejectsNull() {
            var gv = new GeneratedVersion();
            assertThatNullPointerException().isThrownBy(() -> gv.setTemplate(null));
        }
    }

    @Nested
    @DisplayName("PackageName Tests")
    class PackageNameTests {

        @Test
        @DisplayName("setPackageName accepts empty string for default package")
        void setPackageNameAcceptsEmpty() {
            var gv = new GeneratedVersion();
            assertThatNoException().isThrownBy(() -> gv.setPackageName(""));
            assertThat(gv.getPackageName()).isEmpty();
        }

        @Test
        @DisplayName("setPackageName rejects invalid package")
        void setPackageNameRejectsInvalid() {
            var gv = new GeneratedVersion();
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> gv.setPackageName("com..bad"));
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> gv.setPackageName("123bad"));
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> gv.setPackageName("com.class"));
        }
    }

    @Nested
    @DisplayName("writeTemplate Tests")
    class WriteTemplateTests {

        @TempDir
        File tmpDir;

        @Test
        @DisplayName("classFile is empty before writeTemplate is called")
        void classFileEmptyBeforeWrite() {
            var gv = new GeneratedVersion();
            assertThat(gv.getClassFile()).isEmpty();
        }

        @Test
        @DisplayName("classFile is present after writeTemplate is called")
        void classFilePresentAfterWrite() throws IOException {
            var gv = new GeneratedVersion();
            gv.setProject(project);
            gv.setDirectory(tmpDir);
            gv.writeTemplate(gv.buildTemplate());
            assertThat(gv.getClassFile()).isPresent();
        }

        @Test
        @DisplayName("throws NPE when directory or project is not set")
        void throwsWhenRequiredFieldsNull() {
            var gv = new GeneratedVersion();
            gv.setProject(project);
            var t = gv.buildTemplate();
            assertThatNullPointerException().isThrownBy(() -> gv.writeTemplate(t));
        }

        @Test
        @DisplayName("writeTemplate creates parent directories")
        void writeTemplateCreatesParentDirs() throws IOException {
            var gv = new GeneratedVersion();
            gv.setProject(project);
            gv.setDirectory(tmpDir);
            gv.setPackageName("deep.nested.pkg");
            gv.writeTemplate(gv.buildTemplate());
            assertThat(gv.getClassFile().orElseThrow().getParentFile()).exists();
        }

        @Test
        @DisplayName("writeTemplate throws IOException on write failure")
        void writeTemplateThrowsOnWriteFailure(@TempDir Path tempDir) throws IOException {
            var readOnly = tempDir.resolve("ro");
            Files.createDirectory(readOnly);
            assertTrue(readOnly.toFile().setReadOnly());

            var gv = new GeneratedVersion();
            gv.setProject(project);
            gv.setDirectory(readOnly.toFile());
            var template = gv.buildTemplate();
            assertThatIOException().isThrownBy(() -> gv.writeTemplate(template))
                    .withMessageContaining("Could not create project package directories");
        }

        @Test
        @DisplayName("writeTemplate uses explicit packageName when set")
        void writeTemplateUsesExplicitPackageName() throws IOException {
            var gv = new GeneratedVersion();
            gv.setProject(project);
            gv.setDirectory(tmpDir);
            gv.setPackageName("org.foo");
            gv.writeTemplate(gv.buildTemplate());
            assertThat(gv.getClassFile().orElseThrow().toPath())
                    .startsWith(tmpDir.toPath().resolve("org/foo"));
        }

        @Test
        @DisplayName("writeTemplate uses project package when packageName not set")
        void writeTemplateUsesProjectPackage() throws IOException {
            var gv = new GeneratedVersion();
            gv.setProject(project);
            gv.setDirectory(tmpDir);
            gv.writeTemplate(gv.buildTemplate());
            assertThat(gv.getClassFile().orElseThrow().toPath())
                    .startsWith(tmpDir.toPath().resolve("com/example"));
        }

        @Test
        @DisplayName("writeTemplate writes to root when packageName is empty")
        void writeTemplateWritesToRootWhenPackageEmpty() throws IOException {
            var gv = new GeneratedVersion();
            gv.setProject(project);
            gv.setDirectory(tmpDir);
            gv.setPackageName("");
            gv.writeTemplate(gv.buildTemplate());
            assertThat(gv.getClassFile().orElseThrow().getParentFile())
                    .isEqualTo(tmpDir);
        }

        @Test
        @DisplayName("written class file exists on disk")
        void writtenFileExistsOnDisk() throws IOException {
            var gv = new GeneratedVersion();
            gv.setProject(project);
            gv.setDirectory(tmpDir);
            gv.writeTemplate(gv.buildTemplate());
            assertThat(gv.getClassFile().orElseThrow()).exists();
        }

        @Test
        @DisplayName("written file uses custom className in filename")
        void writtenFileUsesCustomClassName() throws IOException {
            var gv = new GeneratedVersion();
            gv.setProject(project);
            gv.setDirectory(tmpDir);
            gv.setClassName("AppVersion");
            gv.writeTemplate(gv.buildTemplate());
            assertThat(gv.getClassFile().orElseThrow().getName()).startsWith("AppVersion");
        }

        @Test
        @DisplayName("written file uses custom extension when packageName is null")
        void writtenFileUsesCustomExtensionWithoutPackage() throws IOException {
            var gv = new GeneratedVersion();
            gv.setProject(project);
            gv.setDirectory(tmpDir);
            gv.setExtension(".kt");
            gv.writeTemplate(gv.buildTemplate());
            assertThat(gv.getClassFile().orElseThrow().getName()).endsWith(".kt");
        }
    }
}