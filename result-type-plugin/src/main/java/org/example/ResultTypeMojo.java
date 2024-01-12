package org.example;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.stream.Stream;

import static org.apache.maven.plugins.annotations.LifecyclePhase.PROCESS_CLASSES;
import static org.apache.maven.plugins.annotations.ResolutionScope.COMPILE_PLUS_RUNTIME;

@Mojo(name = "process", defaultPhase = PROCESS_CLASSES, requiresDependencyResolution = COMPILE_PLUS_RUNTIME, threadSafe = true)
public class ResultTypeMojo extends AbstractMojo {

    /**
     * Should the plugin be skipped.
     */
    @Parameter(defaultValue = "false", property = "org.example.skip")
    protected boolean skip;

    /**
     * Binaries to work on (typically target/classes).
     */
    @Parameter(defaultValue = "${project.build.outputDirectory}", property = "org.example.classes")
    protected File classes;

    /**
     * Current maven project.
     */
    @Parameter(defaultValue = "${project}", readonly = true)
    protected MavenProject project;

    /**
     * Packaging type, mainly used to skip pom packaging.
     */
    @Parameter(defaultValue = "${project.packaging}", readonly = true)
    protected String packaging;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            getLog().info(getClass().getSimpleName() + " is skipped");
            return;
        }
        if ("pom".equals(packaging)) {
            getLog().info("Skipping modules with packaging pom");
            return;
        }
        if (!classes.isDirectory()) {
            getLog().warn(classes + " is not a directory, skipping. Maybe ensure the bound phase for this plugin.");
            return;
        }
        try (URLClassLoader urlClassLoader = new URLClassLoader(getURLs())) {
            new ResultReplacer(classes, urlClassLoader).process();
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }


    private URL[] getURLs() {
        return Stream.concat(
                        Stream.of(classes),
                        project.getArtifacts().stream().map(Artifact::getFile))
                .map(file -> {
                    try {
                        return file.toURI().toURL();
                    } catch (final MalformedURLException e) {
                        throw new IllegalStateException(e.getMessage());
                    }
                }).toArray(URL[]::new);
    }
}