/*
 * Copyright 2023-2024 the original author or authors.
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
import rife.resources.ResourceFinderClasspath;
import rife.resources.ResourceFinderDirectories;
import rife.resources.ResourceFinderGroup;
import rife.template.Template;
import rife.template.TemplateFactory;
import rife.tools.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * GeneratedVersion data class.
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @since 1.0
 */
public class GeneratedVersion {
    private static final String CLASSNAME = "className";
    private static final String EPOCH = "epoch";
    private static final String MAJOR = "major";
    private static final String MINOR = "minor";
    private static final String PACKAGE_NAME = "packageName";
    private static final String PROJECT = "project";
    private static final String QUALIFIER = "qualifier";
    private static final String REVISION = "revision";
    private static final String VERSION = "version";
    private File classFile_;
    private String className_ = "GeneratedVersion";
    private File directory_;
    private String extension_ = ".java";
    private String packageName_;
    private String projectName_;
    private BaseProject project_;
    private File template_;

    /**
     * Builds the template based on the {@link GeneratedVersion} data.
     *
     * @return the template
     */
    public Template buildTemplate() {
        Template template;
        var version = project_.version();
        TemplateFactory.TXT.resetClassLoader();
        if (template_ == null) {
            var group = new ResourceFinderGroup().add(ResourceFinderClasspath.instance());
            template = TemplateFactory.TXT.setResourceFinder(group).get("default_generated_version");
        } else {
            File parent;
            if (template_.getParentFile() != null) {
                parent = template_.getParentFile();
            } else {
                parent = new File(template_.getAbsolutePath()).getParentFile();
            }
            var group = new ResourceFinderGroup().add(new ResourceFinderDirectories(parent));
            template = TemplateFactory.TXT.setResourceFinder(group).get(template_.getName());
        }

        if (packageName_ == null) {
            packageName_ = project_.pkg();
        }

        if (template.hasValueId(PACKAGE_NAME)) {
            template.setValue(PACKAGE_NAME, packageName_);
        }

        if (template.hasValueId(CLASSNAME)) {
            template.setValue(CLASSNAME, className_);
        }

        if (template.hasValueId(PROJECT)) {
            if (projectName_ == null) {
                projectName_ = project_.name();
            }
            template.setValue(PROJECT, projectName_);
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
     * Returns the class file.
     *
     * @return the class file
     */
    public File getClassFile() {
        return classFile_;
    }

    /**
     * Returns the class name.
     *
     * @return the class name
     */
    public String getClassName() {
        return className_;
    }

    /**
     * Returns the destination directory.
     *
     * @return the destination directory
     */
    public File getDirectory() {
        return directory_;
    }

    /**
     * Returns the file extension.
     *
     * @return the file extension
     */
    public String getExtension() {
        return extension_;
    }

    /**
     * Returns the package name.
     *
     * @return the package name
     */
    public String getPackageName() {
        return packageName_;
    }

    /**
     * Returns the project.
     *
     * @return the project
     */
    public BaseProject getProject() {
        return project_;
    }

    /**
     * Returns the project name.
     *
     * @return the project name
     */
    public String getProjectName() {
        return projectName_;
    }

    /**
     * Returns the template.
     *
     * @return the template
     */
    public File getTemplate() {
        return template_;
    }

    /**
     * Sets the class name.
     *
     * @param className the class name
     */
    public void setClassName(String className) {
        this.className_ = className;
    }

    /**
     * Set the destination directory.
     *
     * @param directory the destination directory
     */
    public void setDirectory(File directory) {
        this.directory_ = directory;
    }

    /**
     * Sets the file extension. (Default is: {@code .java})
     *
     * @param extension the file extension
     */
    public void setExtension(String extension) {
        this.extension_ = extension;
    }

    /**
     * Sets the package name.
     *
     * @param packageName the package name
     */
    public void setPackageName(String packageName) {
        this.packageName_ = packageName;
    }

    /**
     * Sets the project.
     *
     * @param project the project
     */
    public void setProject(BaseProject project) {
        this.project_ = project;
    }

    /**
     * Sets the project name.
     *
     * @param projectName the project name
     */
    public void setProjectName(String projectName) {
        this.projectName_ = projectName;
    }

    /**
     * Sets the template file.
     *
     * @param template the template
     */
    public void setTemplate(File template) {
        this.template_ = template;
    }

    /**
     * Writes the project version class in the given directory.
     */
    public void writeTemplate(Template template) throws IOException {
        if (packageName_ != null) {
            classFile_ = Path.of(directory_.getAbsolutePath(), packageName_.replace(".", File.separator),
                    className_ + extension_).toFile();
        } else {
            classFile_ = new File(directory_, className_ + ".java");
        }

        if (!classFile_.getParentFile().exists()) {
            var dirs = classFile_.getParentFile().mkdirs();
            if (!dirs && !classFile_.getParentFile().exists()) {
                throw new IOException("Could not create project package directories: " + classFile_.getParent());
            }
        }

        try {
            FileUtils.writeString(template.getContent(), classFile_);
        } catch (IOException e) {
            throw new IOException("Unable to write the version class file: " + e.getMessage(), e);
        }
    }
}
