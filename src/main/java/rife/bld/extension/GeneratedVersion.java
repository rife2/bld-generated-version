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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import rife.bld.BaseProject;
import rife.bld.extension.tools.IOTools;
import rife.bld.extension.tools.ObjectTools;
import rife.bld.extension.tools.TextTools;
import rife.template.Template;
import rife.tools.FileUtils;

import javax.lang.model.SourceVersion;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * GeneratedVersion data class.
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @since 1.0
 */
@NullMarked
public class GeneratedVersion {

    private static final String CLASS_NAME = "className";
    private static final Pattern CLEAN_TEMPLATE_PATTERN =
            Pattern.compile("(?m)^[ \\t]*\\{\\{v \\w+\\s*/}}[ \\t]*\\R");
    private static final String EPOCH = "epoch";
    private static final String JAVA_EXTENSION = ".java";
    private static final String MAJOR = "major";
    private static final String MINOR = "minor";
    private static final String PACKAGE = "package";
    private static final String PACKAGE_NAME = "packageName";
    private static final String PROJECT = "project";
    private static final String QUALIFIER = "qualifier";
    private static final String REVISION = "revision";
    private static final String VERSION = "version";
    private String className_ = "GeneratedVersion";
    private @Nullable File directory_;
    private String extension_ = JAVA_EXTENSION;
    private @Nullable String packageName_;
    private @Nullable String projectName_;
    private @Nullable BaseProject project_;
    private @Nullable File template_;

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
    public void setClassName(String className) {
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
    @Nullable
    public File getDirectory() {
        return directory_;
    }

    /**
     * Sets the destination directory.
     *
     * @param directory the destination directory
     * @throws NullPointerException if {@code directory} is {@code null}
     */
    public void setDirectory(File directory) {
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
    public void setExtension(String extension) {
        TextTools.requireNotBlank(extension, "extension");
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
    @Nullable
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
    public void setPackageName(String packageName) {
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
    @Nullable
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
    public void setProject(BaseProject project) {
        ObjectTools.requireNonNull(project, PROJECT);
        this.project_ = project;
    }

    /**
     * Returns the project name.
     *
     * @return the project name
     */
    @Nullable
    public String getProjectName() {
        return projectName_;
    }

    /**
     * Sets the project name.
     *
     * @param projectName the project name
     * @throws NullPointerException     if {@code projectName} is {@code null}
     * @throws IllegalArgumentException if {@code projectName} is blank
     */
    public void setProjectName(String projectName) {
        TextTools.requireNotBlank(projectName, "projectName");
        this.projectName_ = projectName;
    }

    /**
     * Returns the template file.
     *
     * @return the template file
     */
    @Nullable
    public File getTemplate() {
        return template_;
    }

    /**
     * Sets the template file.
     *
     * @param template the template
     */
    public void setTemplate(File template) {
        ObjectTools.requireNonNull(template, "template");
        this.template_ = template;
    }

    private String cleanTemplate(Template template) {
        var rendered = template.getContent();
        return CLEAN_TEMPLATE_PATTERN.matcher(rendered).replaceAll("");
    }


    /**
     * Fills the template based on the {@link GeneratedVersion} data.
     *
     * <p>Note: if {@code packageName} or {@code projectName} were not explicitly set,
     * they are resolved from the project for this template only.
     *
     * @return the template
     * @throws NullPointerException if the project has not been set
     */
    Template fillTemplate(Template template) {
        ObjectTools.requireNonNull(template, "template");
        ObjectTools.requireNonNull(project_, PROJECT);

        var version = ObjectTools.requireNonNull(project_.version(), VERSION);

        var resolvedPackage = (packageName_ != null) ? packageName_ : project_.pkg();
        var resolvedProject = (projectName_ != null) ? projectName_ : project_.name();

        // Skip package declaration if blank
        if (template.hasValueId(PACKAGE) && TextTools.isNotBlank(resolvedPackage)) {
            template.setValue(PACKAGE, PACKAGE + ' ' + resolvedPackage + ';');
        }

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
            if (TextTools.isNotBlank(version.qualifier())) {
                template.setValue(QUALIFIER, version.qualifier());
            } else {
                template.blankValue(QUALIFIER);
            }
        }

        return template;
    }

    /**
     * Resolves the target file path for a given file name, using the configured
     * directory and package, without writing anything.
     *
     * @param fileName the file name, including extension
     * @return the resolved file
     */
    @SuppressWarnings("SameParameterValue")
    Optional<File> resolveClassFile(String fileName) {
        if (directory_ == null) {
            return Optional.empty();
        }
        var resolvedPackage = (packageName_ != null) ? packageName_
                : (project_ != null ? project_.pkg() : null);
        Path classPath;
        if (TextTools.isNotEmpty(resolvedPackage)) {
            classPath = Path.of(
                    directory_.getAbsolutePath(),
                    resolvedPackage.replace(".", File.separator),
                    fileName
            );
        } else {
            classPath = Path.of(directory_.getAbsolutePath(), fileName);
        }
        return Optional.of(classPath.toFile());
    }

    /**
     * Writes the project version class to the configured directory.
     *
     * @param template the rendered template to write
     * @return {@code Optional<File>} containing the written file if created
     * @throws NullPointerException if the template, project or directory have not been set
     * @throws IOException          if the class file cannot be created or written
     */
    Optional<File> writeTemplate(Template template) throws IOException {
        return writeTemplate(template, className_ + extension_, true);
    }

    /**
     * Writes a class file to the configured directory.
     *
     * @param template  the rendered template to write
     * @param fileName  the class file name, including the extension
     * @param overwrite {@code true} to overwrite the class file if it exists; {@code false} otherwise
     * @return {@code Optional<File>} containing the written file if created or overwritten;
     * {@code Optional.empty()} if skipped because the file exists and {@code overwrite} is false
     * @throws NullPointerException if the template, project or directory have not been set
     * @throws IOException          if the class file cannot be created or written
     */
    Optional<File> writeTemplate(Template template, String fileName, boolean overwrite) throws IOException {
        ObjectTools.requireNonNull(template, "template");
        ObjectTools.requireNonNull(project_, PROJECT);
        ObjectTools.requireNonNull(directory_, "directory");

        var resolvedPackage = (packageName_ != null) ? packageName_ : project_.pkg();

        Path classPath;
        if (TextTools.isNotEmpty(resolvedPackage)) {
            classPath = Path.of(
                    directory_.getAbsolutePath(),
                    resolvedPackage.replace(".", File.separator),
                    fileName
            );
        } else {
            classPath = Path.of(directory_.getAbsolutePath(), fileName);
        }

        var targetFile = classPath.toFile();

        // Skip if file exists and we don't want to overwrite
        if (!overwrite && targetFile.exists()) {
            return Optional.empty(); // skipped, nothing written
        }

        // Ensure parent directories exist before writing
        var parent = targetFile.getParentFile();
        if (parent != null && !parent.exists()) {
            try {
                IOTools.createDirs(parent);
            } catch (IOException e) {
                throw new IOException("Could not create destination directories: " + parent, e);
            }
        }

        try {
            FileUtils.writeString(cleanTemplate(template), targetFile);
        } catch (IOException e) {
            throw new IOException("Unable to write class file: " + targetFile, e);
        }

        return Optional.of(targetFile); // wrote or overwrote
    }
}