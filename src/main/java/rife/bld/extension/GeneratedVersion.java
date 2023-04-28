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

import java.io.File;

/**
 * GeneratedVersion data class.
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @since 1.0
 */
@SuppressWarnings("PMD.DataClass")
public class GeneratedVersion {
    private File classFile;
    private String className;
    private String packageName;
    private BaseProject project;
    private String projectName;
    private File template;

    /**
     * Returns the class file.
     */
    public File getClassFile() {
        return classFile;
    }

    /**
     * Returns the class name.
     */
    public String getClassName() {
        return className;
    }

    /**
     * Returns the package name.
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * Returns the project.
     */
    public BaseProject getProject() {
        return project;
    }

    /**
     * Returns the project name.
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     * Returns the template.
     */
    public File getTemplate() {
        return template;
    }

    /**
     * Sets the class file.
     */
    public void setClassFile(File classFile) {
        this.classFile = classFile;
    }

    /**
     * Sets the class name.
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * Sets the package name.
     */
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    /**
     * Sets the project.
     */
    public void setProject(BaseProject project) {
        this.project = project;
    }

    /**
     * Sets the project name.
     */
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    /**
     * Sets the template file.
     */
    public void setTemplate(File template) {
        this.template = template;
    }
}
