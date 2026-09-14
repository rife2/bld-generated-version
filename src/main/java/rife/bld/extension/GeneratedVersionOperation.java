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
import rife.bld.BaseProject;
import rife.bld.extension.tools.ObjectTools;
import rife.bld.extension.tools.TextTools;
import rife.bld.operations.AbstractOperation;
import rife.bld.operations.exceptions.ExitStatusException;
import rife.resources.ResourceFinderClasspath;
import rife.resources.ResourceFinderDirectories;
import rife.resources.ResourceFinderGroup;
import rife.template.Template;
import rife.template.TemplateFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Generates a project version data class.
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @since 1.0
 */
@NullMarked
public class GeneratedVersionOperation extends AbstractOperation<GeneratedVersionOperation> {

    private static final String GENERATED = "generated";
    private static final Logger logger = Logger.getLogger(GeneratedVersionOperation.class.getName());
    private final GeneratedVersion generatedVersion_ = new GeneratedVersion();
    private boolean generateAnnotation_;

    @Override
    @SuppressWarnings("PMD.PreserveStackTrace")
    @SuppressFBWarnings({"LEST_LOST_EXCEPTION_STACK_TRACE", "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE"})
    public void execute() throws ExitStatusException {
        ObjectTools.requireNonNull(generatedVersion_.getProject(), "project");
        ObjectTools.requireNonNull(generatedVersion_.getDirectory(), "directory");
        ObjectTools.requireNotEmpty(generatedVersion_.getClassName(), "class name");

        try {
            var versionTemplate = generatedVersion_.fillTemplate(findVersionTemplate());

            createAnnotation(versionTemplate);

            var versionFile = generatedVersion_.writeTemplate(versionTemplate)
                    .orElseThrow(() -> new IOException("Version class could not be written"));

            if (!silent() && logger.isLoggable(Level.INFO)) {
                var project = generatedVersion_.getProject();
                // project is non-null due to requireNonNull above, version() may be null -> String.valueOf is null-safe
                var version = String.valueOf(project.version());
                logger.log(Level.INFO, "Generated version ({0}) class saved to: {1}",
                        new Object[]{version, versionFile.toURI()});
            }
        } catch (IOException e) {
            if (!silent() && logger.isLoggable(Level.SEVERE)) {
                logger.log(Level.SEVERE, "Failed to generate version class", e);
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
    public GeneratedVersionOperation className(String className) {
        generatedVersion_.setClassName(className);
        return this;
    }

    /**
     * Sets the class template path.
     *
     * @param template the template path
     * @return this operation instance
     * @throws NullPointerException     if {@code template} is {@code null}
     * @throws IllegalArgumentException if {@code template} is blank
     */
    public GeneratedVersionOperation classTemplate(String template) {
        TextTools.requireNotBlank(template, "template");
        return classTemplate(new File(template));
    }

    /**
     * Sets the class template path.
     *
     * @param template the template path
     * @return this operation instance
     * @throws NullPointerException if {@code template} is {@code null}
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
     * @throws NullPointerException if {@code template} is {@code null}
     */
    public GeneratedVersionOperation classTemplate(Path template) {
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
    public GeneratedVersionOperation directory(String directory) {
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
    public GeneratedVersionOperation directory(File directory) {
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
    public GeneratedVersionOperation directory(Path directory) {
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
    public GeneratedVersionOperation extension(String extension) {
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
    public GeneratedVersionOperation fromProject(BaseProject project) {
        ObjectTools.requireNonNull(project, "project");
        generatedVersion_.setProject(project);
        generatedVersion_.setDirectory(project.srcMainJavaDirectory());
        return this;
    }

    /**
     * Determines if the {@code Generated} annotation class file should be created.
     * <p>
     * The class file is not overwritten if it already exists.
     *
     * @param generate {@code true} to generate the annotation; {@code false} otherwise
     * @return this operation instance
     */
    public GeneratedVersionOperation generateAnnotation(boolean generate) {
        generateAnnotation_ = generate;
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
    @SuppressFBWarnings(value = "EI_EXPOSE_REP",
            justification = "Intentional: caller receives a mutable reference by design.")
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
    public GeneratedVersionOperation packageName(String packageName) {
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
    public GeneratedVersionOperation projectName(String projectName) {
        generatedVersion_.setProjectName(projectName);
        return this;
    }

    private void createAnnotation(Template versionTemplate) throws IOException {
        if (!generateAnnotation_) {
            return;
        }

        var annotationTemplate =
                generatedVersion_.fillTemplate(findResourceTemplate("generated_annotation"));

        Optional<File> annotationFile =
                generatedVersion_.writeTemplate(annotationTemplate, "Generated.java", false);

        if (!silent() && logger.isLoggable(Level.INFO)) {
            annotationFile.ifPresent(file ->
                    logger.log(Level.INFO, "Generated annotation class saved to: {0}", file.toURI()));
        }

        var annotationTarget = annotationFile
                .or(() -> generatedVersion_.resolveClassFile("Generated.java"))
                .filter(File::exists);

        if (annotationTarget.isPresent() && versionTemplate.hasValueId(GENERATED)) {
            versionTemplate.setValue(GENERATED, "@Generated");
        }
    }

    /**
     * Find the given template in the project resources.
     *
     * @param templateName the template name to find
     * @return the found template
     */
    private Template findResourceTemplate(String templateName) {
        var group = new ResourceFinderGroup().add(ResourceFinderClasspath.instance());
        return TemplateFactory.TXT.setResourceFinder(group).get(templateName);
    }

    /**
     * Finds the {@link GeneratedVersion#getTemplate() version template} or use the default template.
     *
     * @return the version templates
     */
    private Template findVersionTemplate() {
        var customTemplate = generatedVersion_.getTemplate();
        if (customTemplate == null) {
            return findResourceTemplate("default_generated_version");
        } else {
            var file = customTemplate.getAbsoluteFile();
            var parentFile = file.getParentFile();
            var parent = parentFile != null ? parentFile : new File(".");
            var group = new ResourceFinderGroup().add(new ResourceFinderDirectories(parent));
            return TemplateFactory.TXT.setResourceFinder(group).get(file.getName());
        }
    }
}