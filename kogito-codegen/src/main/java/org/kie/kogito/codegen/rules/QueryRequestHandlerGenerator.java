/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import org.drools.compiler.compiler.DroolsError;
import org.drools.modelcompiler.builder.QueryModel;
import org.kie.internal.ruleunit.RuleUnitDescription;
import org.kie.kogito.codegen.AddonsConfig;
import org.kie.kogito.codegen.BodyDeclarationComparator;
import org.kie.kogito.codegen.FileGenerator;
import org.kie.kogito.codegen.di.DependencyInjectionAnnotator;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.javaparser.StaticJavaParser.*;
import static org.drools.modelcompiler.builder.generator.DrlxParseUtil.classNameToReferenceType;

public class QueryRequestHandlerGenerator implements FileGenerator {

    private final RuleUnitDescription ruleUnit;
    private final QueryModel query;
    private final DependencyInjectionAnnotator annotator;

    private final String name;
    private final String requestHandler;
    private final String queryClassName;
    private final String targetCanonicalName;
    private final String generatedFilePath;
    private final AddonsConfig addonsConfig;

    public QueryRequestHandlerGenerator(RuleUnitDescription ruleUnit, QueryModel query, DependencyInjectionAnnotator annotator, AddonsConfig addonsConfig) {
        this.ruleUnit = ruleUnit;
        this.query = query;
        this.name = toCamelCase(query.getName());
        this.requestHandler = toKebabCase(name);
        this.annotator = annotator;

        this.queryClassName = ruleUnit.getSimpleName() + "Query" + name;
        this.targetCanonicalName = queryClassName + "RequestHandler";
        this.generatedFilePath = (query.getNamespace() + "." + targetCanonicalName).replace('.', '/') + ".java";
        this.addonsConfig = addonsConfig;
    }

    public QueryGenerator getQueryGenerator() {
        return new QueryGenerator(ruleUnit, query, name);
    }

    @Override
    public String generatedFilePath() {
        return generatedFilePath;
    }

    @Override
    public boolean validate() {
        return !query.getBindings().isEmpty();
    }

    @Override
    public DroolsError getError() {
        if (query.getBindings().isEmpty()) {
            return new NoBindingQuery( query );
        }
        return null;
    }

    public static class NoBindingQuery extends DroolsError {
        private static final int[] ERROR_LINES = new int[0];

        private final QueryModel query;

        public NoBindingQuery( QueryModel query ) {
            this.query = query;
        }

        @Override
        public String getMessage() {
            return "Query " + query.getName() + " has no bound variable. At least one binding is required to determine the value returned by this query";
        }

        @Override
        public int[] getLines() {
            return ERROR_LINES;
        }
    }

    @Override
    public String generate() {
        CompilationUnit cu = parse(
                this.getClass().getResourceAsStream("/class-templates/rules/RequestHandlerQueryTemplate.java"));
        cu.setPackageDeclaration(query.getNamespace());

        ClassOrInterfaceDeclaration clazz = cu
                .findFirst(ClassOrInterfaceDeclaration.class)
                .orElseThrow(() -> new NoSuchElementException("Compilation unit doesn't contain a class or interface declaration!"));
        clazz.setName(targetCanonicalName);

        cu.findAll(StringLiteralExpr.class).forEach(this::interpolateStrings);

        FieldDeclaration ruleUnitDeclaration = clazz
                .getFieldByName("ruleUnit")
                .orElseThrow(() -> new NoSuchElementException("ClassOrInterfaceDeclaration doesn't contain a field named ruleUnit!"));
        setUnitGeneric(ruleUnitDeclaration.getElementType());
        if (annotator != null) {
            annotator.withInjection(ruleUnitDeclaration);
        }

        String returnType = getReturnType(clazz);
        generateConstructors(clazz);
        generateInterfaces(clazz, returnType);
        generateHandleRequestMethods(cu, clazz, returnType);
        clazz.getMembers().sort(new BodyDeclarationComparator());
        return cu.toString();
    }

    public String getRequestHandlerName() {
        return requestHandler;
    }

    private void generateConstructors(ClassOrInterfaceDeclaration clazz) {
        for (ConstructorDeclaration c : clazz.getConstructors()) {
            c.setName(targetCanonicalName);
            if (!c.getParameters().isEmpty()) {
                setUnitGeneric(c.getParameter(0).getType());
            }
        }
    }

    private void generateInterfaces(ClassOrInterfaceDeclaration clazz, String returnType) {
        ClassOrInterfaceType implementedTypes = clazz.getImplementedTypes(0);
        implementedTypes.asClassOrInterfaceType().setTypeArguments(classNameToReferenceType(ruleUnit.getCanonicalName()),
                classNameToReferenceType(returnType));
    }

    private void generateHandleRequestMethods(CompilationUnit cu, ClassOrInterfaceDeclaration clazz, String returnType) {
        boolean hasDI = annotator != null;

        MethodDeclaration handleRequestMethod = clazz.getMethodsByName("handleRequest").get(0);
        handleRequestMethod.getParameter(0).setType(ruleUnit.getCanonicalName() + (hasDI ? "" : "DTO"));
        handleRequestMethod.setType(toNonPrimitiveType(returnType));

        Statement statement = handleRequestMethod
                .getBody()
                .orElseThrow(() -> new NoSuchElementException("A method declaration doesn't contain a body!"))
                .getStatement(0);
        statement.findAll(VariableDeclarator.class).forEach(decl -> setUnitGeneric(decl.getType()));
        statement.findAll( MethodCallExpr.class ).forEach(m -> m.addArgument( hasDI ? "unitDTO" : "unitDTO.get()" ) );

        Statement secondStatementListResults = handleRequestMethod
                .getBody()
                .orElseThrow(() -> new NoSuchElementException("A method declaration doesn't contain a body!"))
                .getStatement(1);
        secondStatementListResults.findAll(VariableDeclarator.class).forEach(decl -> setGeneric(decl.getType(), returnType));
        secondStatementListResults.findAll(ClassExpr.class).forEach(expr -> expr.setType( queryClassName ) );

        Statement returnMethodSingle = handleRequestMethod
                .getBody()
                .orElseThrow(() -> new NoSuchElementException("A method declaration doesn't contain a body!"))
                .getStatement(2);
        returnMethodSingle.findAll(VariableDeclarator.class).forEach(decl -> decl.setType(toNonPrimitiveType(returnType)));

        if (addonsConfig.useMonitoring()) {
            addMonitoringToResource(cu, new MethodDeclaration[]{handleRequestMethod}, requestHandler);
        }
    }

    private void addMonitoringToResource(CompilationUnit cu, MethodDeclaration[] methods, String nameURL) {
        cu.addImport(new ImportDeclaration(new Name("org.kie.kogito.monitoring.core.system.metrics.SystemMetricsCollector"), false, false));

        for (MethodDeclaration md : methods) {
            BlockStmt body = md.getBody().orElseThrow(() -> new NoSuchElementException("A method declaration doesn't contain a body!"));
            NodeList<Statement> statements = body.getStatements();
            ReturnStmt returnStmt = body.findFirst(ReturnStmt.class).orElseThrow(() -> new NoSuchElementException("A method declaration doesn't contain a return statement!"));
            statements.addFirst(parseStatement("long startTime = System.nanoTime();"));
            statements.addBefore(parseStatement("long endTime = System.nanoTime();"), returnStmt);
            statements.addBefore(parseStatement("SystemMetricsCollector.registerElapsedTimeSampleMetrics(\"" + nameURL + "\", endTime - startTime);"), returnStmt);
            md.setBody(wrapBodyAddingExceptionLogging(body, nameURL));
        }
    }

    private BlockStmt wrapBodyAddingExceptionLogging(BlockStmt body, String nameURL) {
        TryStmt ts = new TryStmt();
        ts.setTryBlock(body);
        CatchClause cc = new CatchClause();
        String exceptionName = "e";
        cc.setParameter(new Parameter().setName(exceptionName).setType(Exception.class));
        BlockStmt cb = new BlockStmt();
        cb.addStatement(parseStatement(
                String.format(
                        "SystemMetricsCollector.registerException(\"%s\", %s.getStackTrace()[0].toString());",
                        nameURL,
                        exceptionName)
        ));
        cb.addStatement(new ThrowStmt(new NameExpr(exceptionName)));
        cc.setBody(cb);
        ts.setCatchClauses(new NodeList<>(cc));
        return new BlockStmt(new NodeList<>(ts));
    }

    private String getReturnType(ClassOrInterfaceDeclaration clazz) {
        if (query.getBindings().size() == 1) {
            Map.Entry<String, Class<?>> binding = query.getBindings().entrySet().iterator().next();
            return binding.getValue().getCanonicalName();
        }
        return queryClassName + ".Result";
    }

    private void interpolateStrings(StringLiteralExpr vv) {
        String interpolated = vv.getValue()
                .replace("$name$", name)
                .replace("$endpointName$", requestHandler)
                .replace("$queryName$", query.getName())
                .replace("$prometheusName$", requestHandler);
        vv.setString(interpolated);
    }

    private void setUnitGeneric(Type type) {
        setGeneric(type, ruleUnit);
    }

    static void setGeneric(Type type, RuleUnitDescription ruleUnit) {
        type.asClassOrInterfaceType().setTypeArguments(classNameToReferenceType(ruleUnit.getCanonicalName()));
    }

    static void setGeneric(Type type, String typeArgument) {
        type.asClassOrInterfaceType().setTypeArguments(parseClassOrInterfaceType(toNonPrimitiveType(typeArgument)));
    }

    private static String toNonPrimitiveType(String type) {
        switch (type) {
            case "int":
                return "Integer";
            case "long":
                return "Long";
            case "double":
                return "Double";
            case "float":
                return "Float";
            case "short":
                return "Short";
            case "byte":
                return "Byte";
            case "char":
                return "Character";
            case "boolean":
                return "Boolean";
        }
        return type;
    }

    private static String toCamelCase(String inputString) {
        return Stream.of(inputString.split(" "))
                .map(s -> s.length() > 1 ? s.substring(0, 1).toUpperCase() + s.substring(1) : s.substring(0, 1).toUpperCase())
                .collect(Collectors.joining());
    }

    private static String toKebabCase(String inputString) {
        return inputString.replaceAll("(.)(\\p{Upper})", "$1-$2").toLowerCase();
    }
}
