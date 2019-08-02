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

package org.kie.kogito.codegen.rules;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.drools.compiler.compiler.io.memory.MemoryFile;
import org.drools.compiler.compiler.io.memory.MemoryFileSystem;
import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.drools.compiler.kie.builder.impl.KieBuilderImpl;
import org.drools.compiler.kie.builder.impl.MemoryKieModule;
import org.drools.modelcompiler.CanonicalKieModule;
import org.kie.api.KieServices;
import org.kie.kogito.codegen.ApplicationSection;
import org.kie.kogito.codegen.ConfigGenerator;
import org.kie.kogito.codegen.GeneratedFile;
import org.kie.kogito.codegen.Generator;
import org.kie.kogito.codegen.di.DependencyInjectionAnnotator;
import org.kie.kogito.codegen.rules.config.RuleConfigGenerator;

public class RuleCodegen implements Generator {

    private RuleUnitContainerGenerator moduleGenerator;

    public static RuleCodegen ofPath(Path path) {
        return ofPath( path, false );
    }

    public static RuleCodegen ofPath(Path path, boolean oneClassPerRule) {
        KieServices ks = KieServices.Factory.get();
        return new RuleCodegen((KieBuilderImpl) ks.newKieBuilder(path.toFile()), oneClassPerRule, Collections.emptyList());
    }

    public static RuleCodegen ofFiles(Path basePath, Collection<File> files) {
        KieServices ks = KieServices.Factory.get();
        KieBuilderImpl kieBuilder = (KieBuilderImpl) ks.newKieBuilder(basePath.toFile());
        kieBuilder.setEnforceResourceLocation(false);
        return new RuleCodegen(kieBuilder, true, files);
    }

    private final boolean oneClassPerRule;

    private final KieBuilderImpl kieBuilder;
    /**
     * will compile iff returns true for the given file
     */
    private final Predicate<String> fileFilter;

    private DependencyInjectionAnnotator annotator;
    
    private String ruleEventListenersConfigClass = null;

    public RuleCodegen( KieBuilderImpl kieBuilder, boolean oneClassPerRule, Collection<File> files ) {
        this.kieBuilder = kieBuilder;
        this.oneClassPerRule = oneClassPerRule;
        if (files.isEmpty()) {
            this.fileFilter = f -> !f.contains("src" + File.separator + "test" + File.separator + "java")
                    && !f.endsWith("bpmn")
                    && !f.endsWith("bpmn2");
        } else {
            this.fileFilter = fname ->
                    files.stream()
                            .map(File::getPath)
                            .anyMatch(f -> f.contains(fname));
        }
    }
    
    public static String defaultRuleEventListenerConfigClass(String packageName) {
        return packageName + ".RuleEventListenerConfig";
    }

    public void setPackageName(String packageName) {
        this.moduleGenerator = new RuleUnitContainerGenerator(packageName);
    }

    private MemoryFileSystem getMemoryFileSystem(InternalKieModule kieModule) {
        return kieModule instanceof CanonicalKieModule ?
                ((MemoryKieModule) ((CanonicalKieModule) kieModule).getInternalKieModule()).getMemoryFileSystem() :
                ((MemoryKieModule) kieModule).getMemoryFileSystem();
    }

    public List<GeneratedFile> generate() {
        if (ruleEventListenersConfigClass != null) {
            moduleGenerator.setRuleEventListenersConfigClass(ruleEventListenersConfigClass);
        }
        
        kieBuilder.buildAll(
                (km, cl) ->
                        new RuleCodegenProject(km, cl, annotator)                                
                                .withModuleGenerator(moduleGenerator)
                                .withOneClassPerRule(oneClassPerRule),
                fileFilter
        );

        InternalKieModule kieModule = (InternalKieModule) kieBuilder.getKieModule();

        MemoryFileSystem mfs = getMemoryFileSystem(kieModule);

        return kieModule.getFileNames()
                .stream()
                .filter(f -> f.endsWith("java"))
                .map(mfs::getFile)
                .map(MemoryFile.class::cast)
                .map(f -> new GeneratedFile(GeneratedFile.Type.RULE, f.getPath().toPortableString(), mfs.getFileContents(f)))
                .collect(Collectors.toList());
    }

    @Override
    public void updateConfig(ConfigGenerator cfg) {
        // no config yet
        cfg.withRuleConfig(new RuleConfigGenerator().ruleEventListenersConfig(moduleGenerator.ruleEventListenersConfigClass()));
    }

    public RuleUnitContainerGenerator moduleGenerator() {
        return moduleGenerator;
    }
    
    public RuleCodegen withRuleEventListenersConfig(String ruleEventListenersConfigClass) {
        this.ruleEventListenersConfigClass = ruleEventListenersConfigClass;
        return this;
    }

    public void setDependencyInjection(DependencyInjectionAnnotator annotator) {
        this.annotator = annotator;
    }

    @Override
    public ApplicationSection section() {
        return moduleGenerator;
    }
}
