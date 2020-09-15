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

package org.kie.kogito.codegen.decision;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import org.drools.core.util.IoUtils;
import org.kie.kogito.codegen.AbstractApplicationSection;
import org.kie.kogito.codegen.AddonsConfig;
import org.kie.kogito.codegen.io.CollectedResource;
import org.kie.kogito.decision.DecisionModels;
import org.kie.kogito.dmn.DmnExecutionIdSupplier;

import static org.kie.kogito.codegen.CodegenUtils.newObject;

public class DecisionContainerGenerator extends AbstractApplicationSection {

    private static final String TEMPLATE_JAVA = "/class-templates/DecisionContainerTemplate.java";

    private String applicationCanonicalName;
    private final List<CollectedResource> resources;
    private AddonsConfig addonsConfig = AddonsConfig.DEFAULT;

    public DecisionContainerGenerator(String applicationCanonicalName, List<CollectedResource> cResources) {
        super("DecisionModels", "decisionModels", DecisionModels.class);
        this.applicationCanonicalName = applicationCanonicalName;
        this.resources = cResources;
    }

    public DecisionContainerGenerator withAddons(AddonsConfig addonsConfig) {
        this.addonsConfig = addonsConfig;
        return this;
    }

    @Override
    public ClassOrInterfaceDeclaration classDeclaration() {
        CompilationUnit clazz = StaticJavaParser.parse(this.getClass().getResourceAsStream(TEMPLATE_JAVA));
        ClassOrInterfaceDeclaration typeDeclaration = (ClassOrInterfaceDeclaration) clazz.getTypes().get(0);
        ClassOrInterfaceType applicationClass = StaticJavaParser.parseClassOrInterfaceType(applicationCanonicalName);
        for (CollectedResource resource : resources) {
            MethodCallExpr getResAsStream = getReadResourceMethod(applicationClass, resource);
            MethodCallExpr isr = new MethodCallExpr("readResource").addArgument(getResAsStream);
            Optional<FieldDeclaration> dmnRuntimeField = typeDeclaration.getFieldByName("dmnRuntime");
            Optional<Expression> initalizer = dmnRuntimeField.flatMap(x -> x.getVariable(0).getInitializer());
            if (initalizer.isPresent()) {
                initalizer.get().asMethodCallExpr().addArgument(isr);
            } else {
                throw new RuntimeException("The template " + TEMPLATE_JAVA + " has been modified.");
            }
        }

        if (addonsConfig.useTracing()) {
            setupExecIdSupplierVariable(typeDeclaration);
        }
        return typeDeclaration;
    }

    private String getDecisionModelJarResourcePath(CollectedResource resource) {
        return resource.resource().getSourcePath();
    }

    private String getDecisionModelRelativeResourcePath(CollectedResource resource) {
        String source = getDecisionModelJarResourcePath(resource);
        try {
            Path sourcePath = Paths.get(source).toAbsolutePath().toRealPath();
            Path relativizedPath = resource.basePath().toAbsolutePath().toRealPath().relativize(sourcePath);
            return "/" + relativizedPath.toString().replace(File.separatorChar, '/');
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void setupExecIdSupplierVariable(ClassOrInterfaceDeclaration typeDeclaration) {
        VariableDeclarator execIdSupplierVariable = typeDeclaration.getFieldByName("execIdSupplier")
                .map(x -> x.getVariable(0))
                .orElseThrow(() -> new RuntimeException("Can't find \"execIdSupplier\" field in " + TEMPLATE_JAVA));
        execIdSupplierVariable.setInitializer(newObject(DmnExecutionIdSupplier.class));
    }

    private MethodCallExpr getReadResourceMethod(ClassOrInterfaceType applicationClass, CollectedResource resource) {
        if (resource.basePath().toString().endsWith(".jar")) {
            return new MethodCallExpr(
                    new MethodCallExpr(new NameExpr(IoUtils.class.getCanonicalName() + ".class"), "getClassLoader"),
                    "getResourceAsStream").addArgument(new StringLiteralExpr(getDecisionModelJarResourcePath(resource)));
        }

        return new MethodCallExpr(new FieldAccessExpr(applicationClass.getNameAsExpression(), "class"), "getResourceAsStream")
                .addArgument(new StringLiteralExpr(getDecisionModelRelativeResourcePath(resource)));
    }
}