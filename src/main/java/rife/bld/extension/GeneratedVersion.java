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

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import rife.bld.BaseProject;
import rife.bld.extension.testing.VisibleForTesting;
import rife.bld.extension.tools.IOTools;
import rife.bld.extension.tools.ObjectTools;
import rife.bld.extension.tools.TextTools;
import rife.resources.ResourceFinderClasspath;
import rife.resources.ResourceFinderDirectories;
import rife.resources.ResourceFinderGroup;
import rife.template.Template;
import rife.template.TemplateFactory;
import rife.tools.FileUtils;

import javax.lang.model.SourceVersion;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

/**
 * GeneratedVersion data class.
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @since 1.0
 */
public class GeneratedVersion {

    private static final String CLASS_NAME = "className";
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
     * Returns the class name.
     *
     * @return the class name
     */
    public String getClassName() {
        return className_;
    }

    /**
     * Sets the class name.
     *
     * @param className the class name
     * @throws NullPointerException     if {@code className} is {@code null}
     * @throws IllegalArgumentException if {@code className} is empty or invalid
     */
    public void setClassName(@NonNull String className) {
        ObjectTools.requireNotEmpty(className, CLASS_NAME);
        if (!SourceVersion.isIdentifier(className) || SourceVersion.isKeyword(className)) {
            throw new IllegalArgumentException("Invalid class name: " + className);
        }
        this.className_ = className;
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
     * Sets the destination directory.
     *
     * @param directory the destination directory
     * @throws NullPointerException if {@code directory} is {@code null}
     */
    public void setDirectory(@NonNull File directory) {
        ObjectTools.requireNonNull(directory, "directory");
        this.directory_ = directory;
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
     * Sets the file extension. (Default is: {@code .java})
     *
     * <p>The extension is stripped of leading/trailing whitespace and must start with a {@code .}
     * followed by at least one non-whitespace character (e.g., {@code .java}, {@code .kt}).
     *
     * @param extension the file extension
     * @throws NullPointerException     if {@code extension} is {@code null}
     * @throws IllegalArgumentException if {@code extension} after trimming is not a {@code .}
     *                                  followed by at least one character
     */
    public void setExtension(@NonNull String extension) {
        ObjectTools.requireNonNull(extension, "extension");
        var trimmed = extension.strip();
        if (!trimmed.startsWith(".") || trimmed.length() < 2) {
            throw new IllegalArgumentException(
                    "Extension must be '.' followed by at least one character after trimming, e.g. '.java'");
        }
        this.extension_ = trimmed;
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
     * Sets the package name.
     *
     * @param packageName the package name
     * @throws NullPointerException     if {@code packageName} is {@code null}
     * @throws IllegalArgumentException if {@code packageName} is invalid
     */
    public void setPackageName(@NonNull String packageName) {
        ObjectTools.requireNonNull(packageName, PACKAGE_NAME);
        if (!packageName.isEmpty() && !SourceVersion.isName(packageName)) {
            throw new IllegalArgumentException("Invalid package name: " + packageName);
        }
        this.packageName_ = packageName;
    }


    /**
     * Returns the project.
     *
     * @return the project
     */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP",
            justification = "Caller retains ownership of the project reference.")
    public BaseProject getProject() {
        return project_;
    }

    /**
     * Sets the project.
     *
     * @param project the project
     */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2",
            justification = "Intentional: caller retains ownership of the project reference.")
    public void setProject(@NonNull BaseProject project) {
        ObjectTools.requireNonNull(project, PROJECT);
        this.project_ = project;
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
     * Sets the project name.
     *
     * @param projectName the project name
     */
    public void setProjectName(@NonNull String projectName) {
        ObjectTools.requireNonNull(projectName, "projectName");
        this.projectName_ = projectName;
    }

    /**
     * Returns the template file.
     *
     * @return the template file
     */
    public File getTemplate() {
        return template_;
    }

    /**
     * Sets the template file.
     *
     * @param template the template
     */
    public void setTemplate(@NonNull File template) {
        ObjectTools.requireNonNull(template, "template");
        this.template_ = template;
    }

    /**
     * Builds the template based on the {@link GeneratedVersion} data.
     *
     * <p>Note: if {@code packageName} or {@code projectName} were not explicitly set,
     * they are resolved from the project for this template only.
     *
     * @return the template
     * @throws NullPointerException if the project has not been set
     */
    @VisibleForTesting
    Template buildTemplate() {
        ObjectTools.requireNonNull(project_, PROJECT);

        var version = project_.version();
        TemplateFactory.TXT.resetClassLoader();

        Template template;
        if (template_ == null) {
            var group = new ResourceFinderGroup().add(ResourceFinderClasspath.instance());
            template = TemplateFactory.TXT.setResourceFinder(group).get("default_generated_version");
        } else {
            var parent = template_.getAbsoluteFile().getParentFile();
            var group = new ResourceFinderGroup().add(new ResourceFinderDirectories(parent));
            template = TemplateFactory.TXT.setResourceFinder(group).get(template_.getName());
        }

        var resolvedPackage = (packageName_ != null) ? packageName_ : project_.pkg();
        var resolvedProject = (projectName_ != null) ? projectName_ : project_.name();

        if (template.hasValueId(PACKAGE_NAME)) {
            template.setValue(PACKAGE_NAME, resolvedPackage);
        }

        if (template.hasValueId(CLASS_NAME)) {
            template.setValue(CLASS_NAME, className_);
        }

        if (template.hasValueId(PROJECT)) {
            template.setValue(PROJECT, resolvedProject);
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
     * Returns the class file, or empty if {@link #writeTemplate} has not yet been called.
     *
     * @return the class file, or {@link Optional#empty()} if not yet written
     */
    protected Optional<File> getClassFile() {
        return Optional.ofNullable(classFile_);
    }

    /**
     * Writes the project version class to the configured directory.
     *
     * @param template the rendered template to write
     * @throws IOException if the class file cannot be created or written
     */
    protected void writeTemplate(Template template) throws IOException {
        var resolvedPackage = (packageName_ != null) ? packageName_ : project_.pkg();

        Path classPath;
        if (TextTools.isNotEmpty(resolvedPackage)) {
            classPath = Path.of(
                    directory_.getAbsolutePath(),
                    resolvedPackage.replace(".", File.separator),
                    className_ + extension_
            );
        } else {
            classPath = Path.of(directory_.getAbsolutePath(), className_ + extension_);
        }
        classFile_ = classPath.toFile();

        var parent = classFile_.getParentFile();
        try {
            IOTools.createDirs(parent);
        } catch (IOException e) {
            throw new IOException("Could not create project package directories: " + parent, e);
        }

        try {
            FileUtils.writeString(template.getContent(), classFile_);
        } catch (IOException e) {
            throw new IOException("Unable to write the version class file: " + classFile_, e);
        }
    }
}