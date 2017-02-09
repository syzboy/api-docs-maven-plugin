package com.appdirect;

import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.name;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
import static org.twdata.maven.mojoexecutor.MojoExecutor.version;

import java.io.File;
import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import com.appdirect.vo.API;
import com.appdirect.vo.APIDocConfig;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Make all plugins parameter configurable, even the versions
 */
@Slf4j
@Mojo(name = "generate", defaultPhase = LifecyclePhase.PROCESS_SOURCES, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class MyMojo extends AbstractMojo {
    @Component
    private MavenProject mavenProject;
    @Component
    private MavenSession mavenSession;
    @Component
    private BuildPluginManager pluginManager;

    /**
     * The APIDocs plugin configuration file.
     */
    @Parameter
    protected File configFile = null;

    public void execute() {
        APIDocConfig config = parseConfig(configFile);
        log.info(config.toString());

        for(API api: config.getApis()) {
            String enunciateFileName = new File(config.getEnunciate().getConfigFileRootPath(), api.getRelativeConfigFileName()).toString();
            executeEnunciate(config.getEnunciate().getDocsDir(), enunciateFileName);
            executeSwagger2Markup(enunciateFileName);
            executeAsciiDoctor(enunciateFileName, api);
        }
    }

    private void executeEnunciate(String docsDir, String enunciateFileName) {
        try {
            String docsSubDir = new File(enunciateFileName).getName().replace(".xml", "");
            log.info("Executing Enunciate with docsDir={}, configFile={}, docsSubdir={}", docsDir, enunciateFileName, docsSubDir);
            executeMojo(
                plugin(
                    groupId("com.webcohesion.enunciate"),
                    artifactId("enunciate-maven-plugin"),
                    version("2.3.0")
                ),
                goal("assemble"),
                configuration(
                    element(name("docsDir"), docsDir),
                    element(name("configFile"), enunciateFileName),
                    element(name("docsSubdir"), docsSubDir)
                ),
                executionEnvironment(
                    mavenProject,
                    mavenSession,
                    pluginManager
                )
            );
        } catch (MojoExecutionException e) {
            log.error("Failed to run Enunciate with config file: " + enunciateFileName, e);
        }
    }

    private void executeSwagger2Markup(String enunciateFileName) {
        String name = new File(enunciateFileName).getName().replace(".xml", "");
        String inputDirectory = String.format("${project.build.directory}/documentation/generated/%s/ui", name);
        String outputDirectory = String.format("${project.build.directory}/documentation/generated/%s-asciidoc", name);
        String examplesDirectory = String.format("${project.build.directory}/documentation/generated/%s-generated-snippets", name);
        try {
            executeMojo(
                plugin(
                    groupId("io.github.robwin"),
                    artifactId("swagger2markup-maven-plugin"),
                    version("0.9.3")
                ),
                goal("process-swagger"),
                configuration(
                    element(name("markupLanguage"), "asciidoc"),
                    element(name("swaggerFile"), "swagger.json"),
                    element(name("separateDefinitions"), "true"),
                    element(name("pathsGroupedBy"), "TAGS"),
                    element(name("inputDirectory"), inputDirectory),
                    element(name("outputDirectory"), outputDirectory),
                    element(name("examplesDirectory"), examplesDirectory)
                ),
                executionEnvironment(
                    mavenProject,
                    mavenSession,
                    pluginManager
                )
            );
        } catch (MojoExecutionException e) {
            log.error("Failed to run swagger2markup with inputDirectory={}, outputDirectory={}, examplesDirectory={}.", inputDirectory, outputDirectory, examplesDirectory, e);
        }
    }

    private void executeAsciiDoctor(String enunciateFileName, API api) {
        String name = new File(enunciateFileName).getName().replace(".xml", "");
        String outputDirectory = String.format("${project.build.directory}/documentation/html/%s-swagger-asciidoc-html", name);
        String generated = String.format("${project.build.directory}/documentation/generated/%s-asciidoc", name);
        String generatedSnippets = String.format("${project.build.directory}/documentation/generated/%s-generated-snippets", name);
        try {
            executeMojo(
                plugin(
                    groupId("org.asciidoctor"),
                    artifactId("asciidoctor-maven-plugin"),
                    version("1.5.3")
                ),
                goal("process-asciidoc"),
                configuration(
                    element(name("headerFooter"), "true"),
                    element(name("sourceDirectory"), "src/main/resources/asciidoc"),
                    element(name("backend"), "html"),
                    element(name("sourceHighlighter"), "highlight.js"),
                    element(name("attributes"),
                        element("stylesheet", "default.css"),
                        element("generated", generated),
                        element("generated-snippets", generatedSnippets),
                        element("api-name", api.getApiName())
                    ),
                    element(name("sourceDocumentName"), "index.adoc"),
                    element(name("outputDirectory"), outputDirectory)
                ),
                executionEnvironment(
                    mavenProject,
                    mavenSession,
                    pluginManager
                )
            );
        } catch (MojoExecutionException e) {
            log.error("Failed to run asciidoctor with outputDirectory={}, generated={}, generated-snippets={}, api-name={}.", outputDirectory, generated, generatedSnippets, api.getApiName(), e);
        }
    }

    private APIDocConfig parseConfig(File file) {
        log.info("Reading config file: " + configFile);
        APIDocConfig config = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            config = mapper.readValue(file, APIDocConfig.class);
        } catch (IOException e) {
            log.error("Cannot read config file: " + file);
        }
        return config;
    }


}
