/*
 *  Copyright 2023 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package rife.bld.extension;


import rife.bld.BaseProject;
import rife.bld.operations.AbstractOperation;
import rife.resources.ResourceFinderDirectories;
import rife.template.Template;
import rife.template.TemplateFactory;
import rife.tools.FileUtils;
import rife.tools.exceptions.FileUtilsErrorException;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implements the GeneratedVersionOperation class.
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
            var files = new ResourceFinderDirectories(new File[]{gv.getTemplate().getParentFile()});
            template = TemplateFactory.TXT.setResourceFinder(files).get(gv.getTemplate().getName());
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

    public static void writeTemplate(Template template, GeneratedVersion gv) {
        Path generatedVersionPath;
        if (gv.getPackageName() != null) {
            generatedVersionPath = Path.of(gv.getProject().srcMainJavaDirectory().getAbsolutePath(),
                    gv.getPackageName().replace(".", File.separator), gv.getClassName());
        } else {
            generatedVersionPath = Path.of(gv.getProject().srcMainJavaDirectory().getAbsolutePath(),
                    gv.getClassName());
        }

        if (generatedVersionPath.getParent().toFile().mkdirs()) {
            try {
                FileUtils.writeString(template.getContent(), generatedVersionPath.toFile());
            } catch (FileUtilsErrorException e) {
                if (LOGGER.isLoggable(Level.SEVERE)) {
                    LOGGER.log(Level.SEVERE, "Unable to write the version class file.", e);
                }
            }
        } else {
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.severe("Could not create project package directories.");
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
     * Sets the class template path
     */
    public GeneratedVersionOperation classTemplate(File template) {
        generatedVersion.setTemplate(template);
        return this;
    }

    @Override
    public void execute() throws Exception {
        if (generatedVersion.getProject() == null && LOGGER.isLoggable(Level.SEVERE)) {
            LOGGER.severe("A project must be specified.");
        }

        var template = buildTemplate(generatedVersion);
        writeTemplate(template, generatedVersion);
    }

    /**
     * Sets the project name.
     */
    public GeneratedVersionOperation fromProject(BaseProject project) {
        generatedVersion.setProject(project);
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
        generatedVersion.setPackageName(projectName);
        return this;
    }
}