/*
 * Copyright 2023 the original author or authors.
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

import org.junit.jupiter.api.Test;
import rife.bld.BaseProject;
import rife.bld.Project;
import rife.bld.dependencies.VersionNumber;
import rife.tools.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Implements the GeneratedVersionTest class.
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @since 1.0
 */
class GeneratedVersionTest {
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

    static void deleteOnExit(File folder) {
        folder.deleteOnExit();
        for (var f : Objects.requireNonNull(folder.listFiles())) {
            if (f.isDirectory()) {
                deleteOnExit(f);
            } else {
                f.deleteOnExit();
            }
        }
    }

    @Test
    void testBuildCustomTemplate() {
        var gv = new GeneratedVersion();
        gv.setProject(PROJECT);
        gv.setTemplate(new File(gv.getProject().srcTestResourcesDirectory().getAbsolutePath(),
                "version_test.txt"));
        gv.setPackageName("com.example.my");
        gv.setProjectName("My App");
        gv.setClassName("MyVersion");

        var t = GeneratedVersionOperation.buildTemplate(gv);

        assertThat(t.getContent()).contains("package com.example.my;").contains("class MyVersion")
                .contains("MAJOR = 2").contains("MINOR = 1").contains("REVISION = 3").contains("QUALIFIER = \"\"")
                .contains("private MyVersion").contains("PROJECT = \"My App\"");
    }

    @Test
    void testBuildTemplate() {
        var gv = new GeneratedVersion();
        gv.setProject(PROJECT);
        var t = GeneratedVersionOperation.buildTemplate(gv);

        assertThat(t).isNotNull();

        assertThat(gv.getProject()).isEqualTo(PROJECT);
        assertThat(gv.getPackageName()).isEqualTo(PROJECT.pkg());
        assertThat(gv.getProjectName()).isEqualTo(PROJECT.name());

        assertThat(t.getContent()).contains("package com.example;").contains("class GeneratedVersion")
                .contains("PROJECT = \"MyExample\";").contains("MAJOR = 2").contains("MINOR = 1")
                .contains("REVISION = 3").contains("QUALIFIER = \"\"").contains("VERSION = \"2.1.3\"")
                .contains("private GeneratedVersion");
    }

    @Test
    void testWriteTemplate() throws IOException {
        var gv = new GeneratedVersion();
        gv.setProject(PROJECT);
        var t = GeneratedVersionOperation.buildTemplate(gv);

        var tmpDir = Files.createTempDirectory("bldGeneratedVersion").toFile();

        GeneratedVersionOperation.writeTemplate(t, tmpDir, gv);

        assertThat(gv.getClassFile()).exists();

        deleteOnExit(tmpDir);

        var versionClass = FileUtils.readString(gv.getClassFile());
        assertThat(versionClass).contains("package com.example;").contains("class GeneratedVersion")
                .contains("MAJOR = 2").contains("MINOR = 1").contains("REVISION = 3")
                .contains("private GeneratedVersion");
    }
}
