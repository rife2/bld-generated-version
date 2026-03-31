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

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link GeneratedVersion} in isolation.
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @since 1.0
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class GeneratedVersionTest {

    private final Project PROJECT = new Project() {
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
        @DisplayName("resolves packageName from project.pkg() when not explicitly set")
        void resolvesPackageNameFromProject() {
            var gv = new GeneratedVersion();
            gv.setProject(PROJECT);
            gv.buildTemplate();
            assertThat(gv.getPackageName()).isEqualTo(PROJECT.pkg());
        }

        @Test
        @DisplayName("resolves projectName from project.name() when not explicitly set")
        void resolvesProjectNameFromProject() {
            var gv = new GeneratedVersion();
            gv.setProject(PROJECT);
            gv.buildTemplate();
            assertThat(gv.getProjectName()).isEqualTo(PROJECT.name());
        }

        @Test
        @DisplayName("retains explicitly set packageName after buildTemplate")
        void retainsExplicitPackageName() {
            var gv = new GeneratedVersion();
            gv.setProject(PROJECT);
            gv.setPackageName("com.custom");
            gv.buildTemplate();
            assertThat(gv.getPackageName()).isEqualTo("com.custom");
        }

        @Test
        @DisplayName("retains explicitly set projectName after buildTemplate")
        void retainsExplicitProjectName() {
            var gv = new GeneratedVersion();
            gv.setProject(PROJECT);
            gv.setProjectName("Custom App");
            gv.buildTemplate();
            assertThat(gv.getProjectName()).isEqualTo("Custom App");
        }

        @Test
        @DisplayName("returns non-null template when project is set")
        void returnsTemplate() {
            var gv = new GeneratedVersion();
            gv.setProject(PROJECT);
            assertThat(gv.buildTemplate()).isNotNull();
        }

        @Test
        @DisplayName("template content includes package name")
        void templateContentContainsPackageName() {
            var gv = new GeneratedVersion();
            gv.setProject(PROJECT);
            var t = gv.buildTemplate();
            assertThat(t.getContent()).contains("package com.example");
        }

        @Test
        @DisplayName("template content includes project name")
        void templateContentContainsProjectName() {
            var gv = new GeneratedVersion();
            gv.setProject(PROJECT);
            var t = gv.buildTemplate();
            assertThat(t.getContent()).contains("PROJECT = \"MyExample\"");
        }

        @Test
        @DisplayName("template content includes correct version parts")
        void templateContentContainsVersionParts() {
            var gv = new GeneratedVersion();
            gv.setProject(PROJECT);
            var t = gv.buildTemplate();
            assertThat(t.getContent()).contains("MAJOR = 2").contains("MINOR = 1").contains("REVISION = 3");
        }

        @Test
        @DisplayName("throws IllegalStateException when project is not set")
        void throwsWhenProjectIsNull() {
            var gv = new GeneratedVersion();
            assertThatIllegalStateException().isThrownBy(gv::buildTemplate);
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

    @Nested
    @DisplayName("Null Safety Tests")
    class NullSafetyTests {

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
            gv.setProject(PROJECT);
            gv.setDirectory(tmpDir);
            gv.writeTemplate(gv.buildTemplate());
            assertThat(gv.getClassFile()).isPresent();
        }

        @Test
        @DisplayName("throws IllegalStateException when directory is not set")
        void throwsWhenDirectoryIsNull() {
            var gv = new GeneratedVersion();
            gv.setProject(PROJECT);
            var t = gv.buildTemplate();
            assertThatIllegalStateException().isThrownBy(() -> gv.writeTemplate(t));
        }

        @Test
        @DisplayName("written class file exists on disk")
        void writtenFileExistsOnDisk() throws IOException {
            var gv = new GeneratedVersion();
            gv.setProject(PROJECT);
            gv.setDirectory(tmpDir);
            gv.writeTemplate(gv.buildTemplate());
            assertThat(gv.getClassFile().orElseThrow()).exists();
        }

        @Test
        @DisplayName("written file is placed in package subdirectory when packageName is set")
        void writtenFilePlacedInPackageSubdirectory() throws IOException {
            var gv = new GeneratedVersion();
            gv.setProject(PROJECT);
            gv.setDirectory(tmpDir);
            gv.setPackageName("com.example");
            gv.writeTemplate(gv.buildTemplate());
            assertThat(gv.getClassFile().orElseThrow().toPath())
                    .startsWith(tmpDir.toPath().resolve("com/example"));
        }

        @Test
        @DisplayName("written file uses custom className in filename")
        void writtenFileUsesCustomClassName() throws IOException {
            var gv = new GeneratedVersion();
            gv.setProject(PROJECT);
            gv.setDirectory(tmpDir);
            gv.setClassName("AppVersion");
            gv.writeTemplate(gv.buildTemplate());
            assertThat(gv.getClassFile().orElseThrow().getName()).startsWith("AppVersion");
        }

        @Test
        @DisplayName("written file uses custom extension when packageName is null")
        void writtenFileUsesCustomExtensionWithoutPackage() throws IOException {
            var gv = new GeneratedVersion();
            gv.setProject(PROJECT);
            gv.setDirectory(tmpDir);
            gv.setExtension(".kt");
            gv.writeTemplate(gv.buildTemplate());
            assertThat(gv.getClassFile().orElseThrow().getName()).endsWith(".kt");
        }
    }
}