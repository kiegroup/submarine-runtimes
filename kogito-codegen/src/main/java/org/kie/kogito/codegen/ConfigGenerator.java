/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.codegen;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Optional;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import org.drools.core.util.StringUtils;
import org.kie.kogito.Addons;
import org.kie.kogito.codegen.decision.config.DecisionConfigGenerator;
import org.kie.kogito.codegen.di.CDIDependencyInjectionAnnotator;
import org.kie.kogito.codegen.di.DependencyInjectionAnnotator;
import org.kie.kogito.codegen.di.SpringDependencyInjectionAnnotator;
import org.kie.kogito.codegen.prediction.config.PredictionConfigGenerator;
import org.kie.kogito.codegen.process.config.ProcessConfigGenerator;
import org.kie.kogito.codegen.rules.config.RuleConfigGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.github.javaparser.StaticJavaParser.parse;
import static org.kie.kogito.codegen.ApplicationGenerator.log;
import static org.kie.kogito.codegen.CodegenUtils.method;
import static org.kie.kogito.codegen.CodegenUtils.newObject;

public class ConfigGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigGenerator.class);
    private static final String RESOURCE_DEFAULT = "/class-templates/config/ApplicationConfigTemplate.java";
    private static final String RESOURCE_CDI = "/class-templates/config/CdiApplicationConfigTemplate.java";
    private static final String RESOURCE_SPRING = "/class-templates/config/SpringApplicationConfigTemplate.java";

    private DependencyInjectionAnnotator annotator;
    private ProcessConfigGenerator processConfig;
    private RuleConfigGenerator ruleConfig;
    private DecisionConfigGenerator decisionConfig;
    private PredictionConfigGenerator predictionConfig;

    private String packageName;
    private final String sourceFilePath;
    private final String targetTypeName;
    private final String targetCanonicalName;
    private final TemplatedGenerator templatedGenerator;

    private ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    public ConfigGenerator(String packageName) {
        this.packageName = packageName;
        this.targetTypeName = "ApplicationConfig";
        this.targetCanonicalName = this.packageName + "." + targetTypeName;
        this.sourceFilePath = targetCanonicalName.replace('.', '/') + ".java";
        this.templatedGenerator = new TemplatedGenerator(packageName,
                                                         targetTypeName,
                                                         RESOURCE_CDI,
                                                         RESOURCE_SPRING,
                                                         RESOURCE_DEFAULT);
    }

    public ConfigGenerator withProcessConfig(ProcessConfigGenerator cfg) {
        this.processConfig = cfg;
        if (this.processConfig != null) {
            this.processConfig.withDependencyInjection(annotator);
        }
        return this;
    }

    public ConfigGenerator withRuleConfig(RuleConfigGenerator cfg) {
        this.ruleConfig = cfg;
        if (this.ruleConfig != null) {
            this.ruleConfig.withDependencyInjection(annotator);
        }
        return this;
    }

    public ConfigGenerator withDecisionConfig(DecisionConfigGenerator cfg) {
        this.decisionConfig = cfg;
        if (this.decisionConfig != null) {
            this.decisionConfig.withDependencyInjection(annotator);
        }
        return this;
    }

    public ConfigGenerator withPredictionConfig(PredictionConfigGenerator cfg) {
        this.predictionConfig = cfg;
        if (this.predictionConfig != null) {
            this.predictionConfig.withDependencyInjection(annotator);
        }
        return this;
    }

    public ConfigGenerator withDependencyInjection(DependencyInjectionAnnotator annotator) {
        this.annotator = annotator;
        this.templatedGenerator.withDependencyInjection(annotator);
        return this;
    }

    public ObjectCreationExpr newInstance() {
        return new ObjectCreationExpr()
                .setType(targetCanonicalName);
    }

    public Collection<GeneratedFile> generate() {
        ArrayList<GeneratedFile> generatedFiles = new ArrayList<>();
        generatedFiles.add(
                new GeneratedFile(GeneratedFile.Type.APPLICATION_CONFIG,
                                  generatedFilePath(),
                                  log(compilationUnit().toString()).getBytes(StandardCharsets.UTF_8)));

        generateProcessConfigDescriptor().ifPresent(generatedFiles::add);
        generateRuleConfigDescriptor().ifPresent(generatedFiles::add);
        generatePredictionConfigDescriptor().ifPresent(generatedFiles::add);
        generateDecisionConfigDescriptor().ifPresent(generatedFiles::add);
        generateBeanConfig().ifPresent(generatedFiles::add);

        return generatedFiles;
    }

    private Optional<GeneratedFile> generateProcessConfigDescriptor() {
        if (processConfig == null) {
            return Optional.empty();
        }
        Optional<CompilationUnit> compilationUnit = processConfig.compilationUnit();
        return compilationUnit.map(c -> new GeneratedFile(GeneratedFile.Type.APPLICATION_CONFIG,
                                                          processConfig.generatedFilePath(),
                                                          log(c.toString()).getBytes(StandardCharsets.UTF_8)));
    }

    private Optional<GeneratedFile> generateRuleConfigDescriptor() {
        if (ruleConfig == null) {
            return Optional.empty();
        }
        Optional<CompilationUnit> compilationUnit = ruleConfig.compilationUnit();
        return compilationUnit.map(c -> new GeneratedFile(GeneratedFile.Type.APPLICATION_CONFIG,
                                                          ruleConfig.generatedFilePath(),
                                                          log(c.toString()).getBytes(StandardCharsets.UTF_8)));
    }

    private Optional<GeneratedFile> generateDecisionConfigDescriptor() {
        if (decisionConfig == null) {
            return Optional.empty();
        }
        Optional<CompilationUnit> compilationUnit = decisionConfig.compilationUnit();
        return compilationUnit.map(c -> new GeneratedFile(GeneratedFile.Type.APPLICATION_CONFIG,
                                                          decisionConfig.generatedFilePath(),
                                                          log(c.toString()).getBytes(StandardCharsets.UTF_8)));
    }

    private Optional<GeneratedFile> generatePredictionConfigDescriptor() {
        if (predictionConfig == null) {
            return Optional.empty();
        }
        Optional<CompilationUnit> compilationUnit = predictionConfig.compilationUnit();
        return compilationUnit.map(c -> new GeneratedFile(GeneratedFile.Type.APPLICATION_CONFIG,
                                                          predictionConfig.generatedFilePath(),
                                                          log(c.toString()).getBytes(StandardCharsets.UTF_8)));
    }

    private Optional<GeneratedFile> generateBeanConfig() {
        ConfigBeanGenerator configBeanGenerator = new ConfigBeanGenerator(packageName);
        configBeanGenerator.withDependencyInjection(annotator);

        Optional<CompilationUnit> compilationUnit = configBeanGenerator.compilationUnit();
        return compilationUnit.map(c -> new GeneratedFile(GeneratedFile.Type.APPLICATION_CONFIG,
                                                          configBeanGenerator.generatedFilePath(),
                                                          log(c.toString()).getBytes(StandardCharsets.UTF_8)));
    }

    public CompilationUnit compilationUnit() {
        CompilationUnit compilationUnit = templatedGenerator.compilationUnit()
                .orElseThrow(() -> new IllegalArgumentException("Invalid Template: No CompilationUnit"));

        ClassOrInterfaceDeclaration cls = compilationUnit.findFirst(ClassOrInterfaceDeclaration.class)
                .orElseThrow(() -> new RuntimeException("ApplicationConfig template class not found"));

        replaceAddonPlaceHolder(cls);

        return compilationUnit;
    }

    private void replaceAddonPlaceHolder(ClassOrInterfaceDeclaration cls) {
        // get the place holder and replace it with a list of the addons that have been found
        NameExpr addonsPlaceHolder =
                cls.findFirst(NameExpr.class, e -> e.getNameAsString().equals("$Addons$")).
                        orElseThrow(() -> new IllegalArgumentException("Invalid Template: Missing $Addons$ placeholder"));

        ObjectCreationExpr addonsList = generateAddonsList();
        addonsPlaceHolder.getParentNode().get()
                .replace(addonsPlaceHolder, addonsList);
    }

    private ObjectCreationExpr generateAddonsList() {
        Collection<String> addons = loadAddonList();

        MethodCallExpr asListOfAddons = new MethodCallExpr(new NameExpr("java.util.Arrays"), "asList");
        for (String addon : addons) {
            asListOfAddons.addArgument(new StringLiteralExpr(addon));
        }

        return newObject(Addons.class, asListOfAddons);
    }

    private Collection<String> loadAddonList() {
        ArrayList<String> addons = new ArrayList<>();
        try {
            Enumeration<URL> urls = classLoader.getResources("META-INF/kogito.addon");
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                String addon = StringUtils.readFileAsString(new InputStreamReader(url.openStream()));
                addons.add(addon);
            }
        } catch (IOException e) {
            LOGGER.warn("Unexpected exception during loading of kogito.addon files", e);
        }
        return addons;
    }

    public String generatedFilePath() {
        return sourceFilePath;
    }

    public void withClassLoader(ClassLoader projectClassLoader) {
        this.classLoader = projectClassLoader;
    }
}
