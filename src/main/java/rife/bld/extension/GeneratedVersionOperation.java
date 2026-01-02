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
import rife.bld.BaseProject;
import rife.bld.operations.AbstractOperation;
import rife.bld.operations.exceptions.ExitStatusException;

import java.io.File;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Generates a project version data class.
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @since 1.0
 */
public class GeneratedVersionOperation extends AbstractOperation<GeneratedVersionOperation> {
    private static final Logger LOGGER = Logger.getLogger(GeneratedVersionOperation.class.getName());
    private final GeneratedVersion generatedVersion_ = new GeneratedVersion();

    /**
     * Generates a version data class for this project.
     */
    @Override
    @SuppressWarnings({"PMD.PreserveStackTrace", "PMD.AvoidCatchingGenericException"})
    @SuppressFBWarnings("LEST_LOST_EXCEPTION_STACK_TRACE")
    public void execute() throws Exception {
        if (generatedVersion_.getProject() == null) {
            if (LOGGER.isLoggable(Level.SEVERE) && !silent()) {
                LOGGER.severe("A project must be specified.");
            }
            throw new ExitStatusException(ExitStatusException.EXIT_FAILURE);
        } else {
            try {
                var template = generatedVersion_.buildTemplate();
                generatedVersion_.writeTemplate(template);
                if (LOGGER.isLoggable(Level.INFO) && !silent()) {
                    LOGGER.log(Level.INFO, "Generated version ({0}) class saved to: file://{1}",
                            new String[]{generatedVersion_.getProject().version().toString(),
                                    generatedVersion_.getClassFile().toURI().getPath()});
                }
            } catch (Exception e) {
                if (LOGGER.isLoggable(Level.SEVERE) && !silent()) {
                    LOGGER.severe(e.getMessage());
                }
                throw new ExitStatusException(ExitStatusException.EXIT_FAILURE);
            }
        }
    }

    /**
     * Sets the class name.
     *
     * @param className the class name
     * @return this operation instance
     */
    public GeneratedVersionOperation className(String className) {
        generatedVersion_.setClassName(className);
        return this;
    }

    /**
     * Sets the class template path.
     *
     * @param template the template path
     * @return this operation instance
     */
    @SuppressFBWarnings("PATH_TRAVERSAL_IN")
    public GeneratedVersionOperation classTemplate(String template) {
        return classTemplate(new File(template));
    }

    /**
     * Sets the class template path.
     *
     * @param template the template path
     * @return this operation instance
     */
    public GeneratedVersionOperation classTemplate(File template) {
        generatedVersion_.setTemplate(template);
        return this;
    }

    /**
     * Sets the class template path.
     *
     * @param template the template path
     * @return this operation instance
     */
    public GeneratedVersionOperation classTemplate(Path template) {
        return classTemplate(template.toFile());
    }

    /**
     * Sets the destination directory.
     *
     * @param directory the destination directory
     * @return this operation instance
     */
    @SuppressFBWarnings("PATH_TRAVERSAL_IN")
    public GeneratedVersionOperation directory(String directory) {
        return directory(new File(directory));
    }

    /**
     * Sets the destination directory.
     *
     * @param directory the destination directory
     * @return this operation instance
     */
    public GeneratedVersionOperation directory(File directory) {
        generatedVersion_.setDirectory(directory);
        return this;
    }

    /**
     * Sets the destination directory.
     *
     * @param directory the destination directory
     * @return this operation instance
     */
    public GeneratedVersionOperation directory(Path directory) {
        return directory(directory.toFile());
    }

    /**
     * Sets the file extension. (Default is: {@code .java})
     *
     * @param extension the file extension
     * @return this operation instance
     */
    public GeneratedVersionOperation extension(String extension) {
        generatedVersion_.setExtension(extension);
        return this;
    }

    /**
     * Configure the operation from a {@link BaseProject}.
     *
     * @param project the project
     * @return this operation instance
     */
    public GeneratedVersionOperation fromProject(BaseProject project) {
        generatedVersion_.setProject(project);
        generatedVersion_.setDirectory(project.srcMainJavaDirectory());
        return this;
    }

    /**
     * Retrieves the generated version instance.
     *
     * @return the generated version
     */
    @SuppressFBWarnings("EI_EXPOSE_REP")
    public GeneratedVersion generatedVersion() {
        return generatedVersion_;
    }

    /**
     * Sets the package name.
     *
     * @param packageName the package name
     * @return this operation instance
     */
    public GeneratedVersionOperation packageName(String packageName) {
        generatedVersion_.setPackageName(packageName);
        return this;
    }

    /**
     * Sets the project name.
     *
     * @param projectName the project name
     * @return this operation instance
     */
    public GeneratedVersionOperation projectName(String projectName) {
        generatedVersion_.setProjectName(projectName);
        return this;
    }
}
