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

import java.lang.reflect.Field;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import org.kie.api.runtime.KieSession;
import org.kie.internal.ruleunit.RuleUnitDescription;
import org.kie.internal.ruleunit.RuleUnitVariable;
import org.kie.kogito.codegen.BodyDeclarationComparator;
import org.kie.kogito.codegen.FileGenerator;
import org.kie.kogito.conf.DefaultEntryPoint;
import org.kie.kogito.conf.EntryPoint;
import org.kie.kogito.rules.DataSource;
import org.kie.kogito.rules.DataStore;
import org.kie.kogito.rules.DataStream;
import org.kie.kogito.rules.SingletonStore;
import org.kie.kogito.rules.units.AbstractRuleUnitInstance;
import org.kie.kogito.rules.units.EntryPointDataProcessor;

public class RuleUnitInstanceGenerator implements FileGenerator {

    private final String targetTypeName;
    private final String targetCanonicalName;
    private final String generatedFilePath;
    private final RuleUnitDescription ruleUnitDescription;

    public static String qualifiedName(String packageName, String typeName) {
        return packageName + "." + typeName + "RuleUnitInstance";
    }

    public RuleUnitInstanceGenerator(RuleUnitDescription ruleUnitDescription) {
        this.ruleUnitDescription = ruleUnitDescription;
        this.targetTypeName = ruleUnitDescription.getSimpleName() + "RuleUnitInstance";
        this.targetCanonicalName = ruleUnitDescription.getPackageName() + "." + targetTypeName;
        this.generatedFilePath = targetCanonicalName.replace('.', '/') + ".java";
    }

    @Override
    public String generatedFilePath() {
        return generatedFilePath;
    }

    @Override
    public String generate() {
        return compilationUnit().toString();
    }

    public CompilationUnit compilationUnit() {
        CompilationUnit compilationUnit = new CompilationUnit(ruleUnitDescription.getPackageName());
        compilationUnit.getTypes().add(classDeclaration());
        return compilationUnit;
    }

    private MethodDeclaration bindMethod() {
        MethodDeclaration methodDeclaration = new MethodDeclaration();

        BlockStmt methodBlock = new BlockStmt();
        methodDeclaration.setName("bind")
                .addAnnotation( "Override" )
                .addModifier(Modifier.Keyword.PROTECTED)
                .addParameter(KieSession.class.getCanonicalName(), "runtime")
                .addParameter(ruleUnitDescription.getRuleUnitName(), "value")
                .setType(void.class)
                .setBody(methodBlock);

        try {


            for (RuleUnitVariable m : ruleUnitDescription.getUnitVarDeclarations()) {
                String methodName = m.getter();
                String propertyName = m.getName();

                if ( m.isDataSource() ) {

                    if (m.setter() != null) { // if writable and DataSource is null create and set a new one
                        Expression nullCheck = new BinaryExpr(new MethodCallExpr(new NameExpr("value"), methodName), new NullLiteralExpr(), BinaryExpr.Operator.EQUALS);
                        Expression dataSourceSetter = new MethodCallExpr(new NameExpr("value"), m.setter(), new NodeList<>(createDataSourceExpr(m.getBoxedVarType())));
                        methodBlock.addStatement( new IfStmt( nullCheck, new BlockStmt().addStatement( dataSourceSetter ), null ) );
                    }

                    //  value.$method())
                    Expression fieldAccessor =
                            new MethodCallExpr(new NameExpr("value"), methodName);

                    // .subscribe( new EntryPointDataProcessor(runtime.getEntryPoint()) )

                    String entryPointName = getEntryPointName(ruleUnitDescription, propertyName);
                    MethodCallExpr drainInto = new MethodCallExpr(fieldAccessor, "subscribe")
                            .addArgument(new ObjectCreationExpr(null, StaticJavaParser.parseClassOrInterfaceType( EntryPointDataProcessor.class.getName() ), NodeList.nodeList(
                                    new MethodCallExpr(
                                            new NameExpr("runtime"), "getEntryPoint",
                                            NodeList.nodeList(new StringLiteralExpr( entryPointName ))))));

                    methodBlock.addStatement(drainInto);
                }

                MethodCallExpr setGlobalCall = new MethodCallExpr( new NameExpr("runtime"), "setGlobal" );
                setGlobalCall.addArgument( new StringLiteralExpr( propertyName ) );
                setGlobalCall.addArgument( new MethodCallExpr(new NameExpr("value"), methodName) );
                methodBlock.addStatement(setGlobalCall);
            }

        } catch (Exception e) {
            throw new Error(e);
        }

        return methodDeclaration;
    }

    private Expression createDataSourceExpr(Class<?> dsClass) {
        return new MethodCallExpr(new NameExpr(DataSource.class.getCanonicalName()), getCreateDataSourceMethodName(dsClass));
    }

    private String getCreateDataSourceMethodName(Class<?> dsClass) {
        if ( DataStream.class.isAssignableFrom( dsClass ) ) {
            return "createStream";
        }
        if ( DataStore.class.isAssignableFrom( dsClass ) ) {
            return "createStore";
        }
        if ( SingletonStore.class.isAssignableFrom( dsClass ) ) {
            return "createSingleton";
        }
        throw new UnsupportedOperationException( "Unknown DataSOurce class: " + dsClass );
    }


    private String getEntryPointName( RuleUnitDescription ruleUnitDescription, String propertyName ) {
        Class<?> ruleUnitClass = ruleUnitDescription.getRuleUnitClass();
        if (ruleUnitClass == null) {
            return propertyName;
        }
        try {
            // fixme should transfer this config to RuleUnitVariable
            Field dataSourceField = ruleUnitClass.getDeclaredField(propertyName );
            if (dataSourceField.getAnnotation( DefaultEntryPoint.class ) != null) {
                return org.kie.api.runtime.rule.EntryPoint.DEFAULT_NAME;
            }
            EntryPoint epAnn = dataSourceField.getAnnotation( EntryPoint.class );
            if (epAnn != null) {
                return epAnn.value();
            }
        } catch (NoSuchFieldException e) { }
        return propertyName;
    }

    public ClassOrInterfaceDeclaration classDeclaration() {
        String canonicalName = ruleUnitDescription.getRuleUnitName();
        ClassOrInterfaceDeclaration classDecl = new ClassOrInterfaceDeclaration()
                .setName(targetTypeName)
                .addModifier(Modifier.Keyword.PUBLIC);
        classDecl
                .addExtendedType(
                        new ClassOrInterfaceType(null, AbstractRuleUnitInstance.class.getCanonicalName())
                                .setTypeArguments(new ClassOrInterfaceType(null, canonicalName)))
                .addConstructor(Modifier.Keyword.PUBLIC)
                .addParameter(RuleUnitGenerator.ruleUnitType(canonicalName), "unit")
                .addParameter(canonicalName, "value")
                .addParameter(KieSession.class.getCanonicalName(), "session")
                .setBody(new BlockStmt().addStatement(new MethodCallExpr(
                        "super",
                        new NameExpr("unit"),
                        new NameExpr("value"),
                        new NameExpr("session")
                )));
        classDecl.addMember(bindMethod());
        classDecl.getMembers().sort(new BodyDeclarationComparator());
        return classDecl;
    }
}
