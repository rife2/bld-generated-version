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
import rife.bld.extension.tools.ObjectTools;
import rife.bld.operations.AbstractOperation;
import rife.bld.operations.exceptions.ExitStatusException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Generates a project version data class.
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @since 1.0
 */
@SuppressFBWarnings(
        value = "EI_EXPOSE_REP",
        justification = "Intentional: generatedVersion() exposes the mutable delegate by design"
)
public class GeneratedVersionOperation extends AbstractOperation<GeneratedVersionOperation> {

    private static final Logger logger = Logger.getLogger(GeneratedVersionOperation.class.getName());
    private final GeneratedVersion generatedVersion_ = new GeneratedVersion();

    /**
     * Generates a version data class for this project.
     *
     * @throws NullPointerException if the {@link #fromProject(BaseProject) project}, {@link #directory(File) directory}
     *                              or {@link #className(String) className} are {@code null}
     * @throws Exception            when an exception occurs during the execution
     */
    @Override
    @SuppressWarnings("PMD.PreserveStackTrace")
    @SuppressFBWarnings(value = "LEST_LOST_EXCEPTION_STACK_TRACE",
            justification = "Stack trace is preserved in the log; ExitStatusException signals CLI exit code only")
    public void execute() throws Exception {
        ObjectTools.requireNonNull(generatedVersion_.getProject(), "project");
        ObjectTools.requireNonNull(generatedVersion_.getDirectory(), "directory");
        ObjectTools.requireNotEmpty(generatedVersion_.getClassName(), "class name");

        try {
            var template = generatedVersion_.buildTemplate();
            generatedVersion_.writeTemplate(template);
            if (logger.isLoggable(Level.INFO) && !silent()) {
                logger.log(Level.INFO, "Generated version ({0}) class saved to: {1}",
                        new Object[]{generatedVersion_.getProject().version(),
                                generatedVersion_.getClassFile().orElseThrow().toURI()}
                );
            }
        } catch (IOException e) {
            if (logger.isLoggable(Level.SEVERE) && !silent()) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
            throw new ExitStatusException(ExitStatusException.EXIT_FAILURE);
        }
    }

    /**
     * Sets the class name.
     *
     * @param className the class name
     * @return this operation instance
     * @throws NullPointerException     if {@code className} is {@code null}
     * @throws IllegalArgumentException if {@code className} is empty or invalid
     */
    public GeneratedVersionOperation className(@NonNull String className) {
        generatedVersion_.setClassName(className);
        return this;
    }

    /**
     * Sets the class template path.
     *
     * @param template the template path
     * @return this operation instance
     * @throws NullPointerException     if {@code template} is {@code null}
     * @throws IllegalArgumentException if {@code template} is empty
     */
    @SuppressFBWarnings("PATH_TRAVERSAL_IN")
    public GeneratedVersionOperation classTemplate(@NonNull String template) {
        ObjectTools.requireNotEmpty(template, "template");
        return classTemplate(new File(template));
    }

    /**
     * Sets the class template path.
     *
     * @param template the template path
     * @return this operation instance
     * @throws NullPointerException if {@code template} is {@code null}
     */
    public GeneratedVersionOperation classTemplate(@NonNull File template) {
        generatedVersion_.setTemplate(template);
        return this;
    }

    /**
     * Sets the class template path.
     *
     * @param template the template path
     * @return this operation instance
     * @throws NullPointerException if {@code template} is {@code null}
     */
    public GeneratedVersionOperation classTemplate(@NonNull Path template) {
        ObjectTools.requireNonNull(template, "template");
        return classTemplate(template.toFile());
    }

    /**
     * Sets the destination directory.
     *
     * @param directory the destination directory
     * @return this operation instance
     * @throws NullPointerException if {@code directory} is {@code null}
     */
    @SuppressFBWarnings("PATH_TRAVERSAL_IN")
    public GeneratedVersionOperation directory(@NonNull String directory) {
        ObjectTools.requireNonNull(directory, "directory");
        return directory(new File(directory));
    }

    /**
     * Sets the destination directory.
     *
     * @param directory the destination directory
     * @return this operation instance
     * @throws NullPointerException if {@code directory} is {@code null}
     */
    public GeneratedVersionOperation directory(@NonNull File directory) {
        generatedVersion_.setDirectory(directory);
        return this;
    }

    /**
     * Sets the destination directory.
     *
     * @param directory the destination directory
     * @return this operation instance
     * @throws NullPointerException if {@code directory} is {@code null}
     */
    public GeneratedVersionOperation directory(@NonNull Path directory) {
        ObjectTools.requireNonNull(directory, "directory");
        return directory(directory.toFile());
    }

    /**
     * Sets the file extension. (Default is: {@code .java})
     * <p>
     * The extension is stripped of leading/trailing whitespace and must start with a {@code .}
     * followed by at least one non-whitespace character (e.g., {@code .java}, {@code .kt}).
     *
     * @param extension the file extension
     * @return this operation instance
     * @throws NullPointerException     if {@code extension} is {@code null}
     * @throws IllegalArgumentException if {@code extension} after trimming is not a {@code .}
     *                                  followed by at least one character
     */
    public GeneratedVersionOperation extension(@NonNull String extension) {
        generatedVersion_.setExtension(extension);
        return this;
    }

    /**
     * Configure the operation from a {@link BaseProject}.
     * <p>
     * Sets the following {@link GeneratedVersion} options:
     * <ul>
     * <li>The {@link GeneratedVersion#setProject project} to the given project.</li>
     * <li>The {@link GeneratedVersion#setDirectory destination directory} to the
     * {@link BaseProject#srcMainJavaDirectory() project's main java source} directory,
     * if not already set.</li>
     * </ul>
     *
     * @param project the project
     * @return this operation instance
     * @throws NullPointerException if {@code project} is {@code null}
     */
    public GeneratedVersionOperation fromProject(@NonNull BaseProject project) {
        generatedVersion_.setProject(project);
        if (generatedVersion_.getDirectory() == null) {
            generatedVersion_.setDirectory(project.srcMainJavaDirectory());
        }
        return this;
    }

    /**
     * Retrieves the generated version instance.
     * <p>
     * The returned object is intentionally mutable. It provides access to
     * properties not directly exposed by this operation's fluent API.
     *
     * @return the generated version
     */
    public GeneratedVersion generatedVersion() {
        return generatedVersion_;
    }

    /**
     * Sets the package name.
     *
     * @param packageName the package name
     * @return this operation instance
     * @throws NullPointerException if {@code packageName} is {@code null}
     */
    public GeneratedVersionOperation packageName(@NonNull String packageName) {
        generatedVersion_.setPackageName(packageName);
        return this;
    }

    /**
     * Sets the project name.
     *
     * @param projectName the project name
     * @return this operation instance
     * @throws NullPointerException if {@code projectName} is {@code null}
     */
    public GeneratedVersionOperation projectName(@NonNull String projectName) {
        generatedVersion_.setProjectName(projectName);
        return this;
    }
}