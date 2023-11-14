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


import rife.bld.BaseProject;
import rife.bld.operations.AbstractOperation;
import rife.resources.ResourceFinderDirectories;
import rife.template.Template;
import rife.template.TemplateConfig;
import rife.template.TemplateFactory;
import rife.tools.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Generates a project version data class.
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @since 1.0
 */
public class GeneratedVersionOperation extends AbstractOperation<GeneratedVersionOperation> {
    private static final String CLASSNAME = "className";
    private static final String EPOCH = "epoch";
    private static final Logger LOGGER = Logger.getLogger(GeneratedVersionOperation.class.getName());
    private static final String MAJOR = "major";
    private static final String MINOR = "minor";
    private static final String PACKAGE_NAME = "packageName";
    private static final String PROJECT = "project";
    private static final String QUALIFIER = "qualifier";
    private static final String REVISION = "revision";
    private static final String VERSION = "version";
    private final GeneratedVersion generatedVersion = new GeneratedVersion();

    /**
     * Builds the template based on the {@link GeneratedVersion} data.
     */
    public static Template buildTemplate(GeneratedVersion gv) {
        Template template;
        var version = gv.getProject().version();
        if (gv.getTemplate() == null) {
            template = TemplateFactory.TXT.get("version.txt");
        } else {
            var files = new ResourceFinderDirectories(gv.getTemplate().getParentFile());
            template = new TemplateFactory(TemplateConfig.TXT, "txtFiles", TemplateFactory.TXT)
                    .setResourceFinder(files).get(gv.getTemplate().getName());
        }

        if (gv.getPackageName() == null) {
            gv.setPackageName(gv.getProject().pkg());
        }

        if (template.hasValueId(PACKAGE_NAME)) {
            template.setValue(PACKAGE_NAME, gv.getPackageName());
        }

        gv.setClassName(Objects.requireNonNullElse(gv.getClassName(), "GeneratedVersion"));
        if (template.hasValueId(CLASSNAME)) {
            template.setValue(CLASSNAME, gv.getClassName());
        }

        if (template.hasValueId(PROJECT)) {
            if (gv.getProjectName() == null) {
                gv.setProjectName(gv.getProject().name());
            }
            template.setValue(PROJECT, gv.getProjectName());
        }

        if (template.hasValueId(EPOCH)) {
            template.setValue(EPOCH, System.currentTimeMillis());
        }

        if (template.hasValueId(VERSION)) {
            template.setValue(VERSION, version.toString());
        }

        if (template.hasValueId(MAJOR)) {
            template.setValue(MAJOR, version.majorInt());
        }

        if (template.hasValueId(MINOR)) {
            template.setValue(MINOR, version.minorInt());
        }

        if (template.hasValueId(REVISION)) {
            template.setValue(REVISION, version.revisionInt());
        }

        if (template.hasValueId(QUALIFIER)) {
            template.setValue(QUALIFIER, version.qualifier());
        }

        return template;
    }

    /**
     * Writes the project version class in the given directory.
     */
    public static void writeTemplate(Template template, GeneratedVersion gv) {
        if (gv.getPackageName() != null) {
            gv.setClassFile(Path.of(gv.getDirectory().getAbsolutePath(),
                    gv.getPackageName().replace(".", File.separator), gv.getClassName()
                            + gv.getExtension()).toFile());
        } else {
            gv.setClassFile(Path.of(gv.getDirectory().getAbsolutePath(), gv.getClassName() + ".java").toFile());
        }

        if (!gv.getClassFile().getParentFile().exists()) {
            var mkdirs = gv.getClassFile().getParentFile().mkdirs();
            if (!mkdirs && !gv.getClassFile().getParentFile().exists() && LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.log(Level.SEVERE, "Could not create project package directories: {0}",
                        gv.getClassFile().getParent());
            }
        }

        try {
            var updated = gv.getClassFile().exists();
            FileUtils.writeString(template.getContent(), gv.getClassFile());
            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.log(Level.INFO, "Generated version ({0}) class has been {1}: {2}",
                        new String[]{gv.getProject().version().toString(), updated ? "updated" : "created",
                                gv.getClassFile().toString()});
            }
        } catch (IOException e) {
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.log(Level.SEVERE, "Unable to write the version class file.", e);
            }
        }
    }

    /**
     * Sets the class name.
     */
    public GeneratedVersionOperation className(String className) {
        generatedVersion.setClassName(className);
        return this;
    }

    /**
     * Sets the class template path.
     */
    public GeneratedVersionOperation classTemplate(File template) {
        generatedVersion.setTemplate(template);
        return this;
    }

    /**
     * Sets the destination directory.
     */
    public GeneratedVersionOperation directory(File directory) {
        generatedVersion.setDirectory(directory);
        return this;
    }

    /**
     * Generates a version data class for this project.
     */
    @Override
    public void execute() {
        if (generatedVersion.getProject() == null && LOGGER.isLoggable(Level.SEVERE)) {
            LOGGER.severe("A project must be specified.");
        }

        var template = buildTemplate(generatedVersion);
        writeTemplate(template, generatedVersion);
    }

    /**
     * Sets the file extension. (Default is: {@code .java})
     */
    public GeneratedVersionOperation extension(String extension) {
        generatedVersion.setExtension(extension);
        return this;
    }

    /**
     * Configure the operation from a {@link BaseProject}.
     */
    public GeneratedVersionOperation fromProject(BaseProject project) {
        generatedVersion.setProject(project);
        generatedVersion.setDirectory(project.srcMainJavaDirectory());
        return this;
    }

    /**
     * Sets the package name.
     */
    public GeneratedVersionOperation packageName(String packageName) {
        generatedVersion.setPackageName(packageName);
        return this;
    }

    /**
     * Sets the project name.
     */
    public GeneratedVersionOperation projectName(String projectName) {
        generatedVersion.setProjectName(projectName);
        return this;
    }
}