/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.serverless.workflow.suppliers;

import java.util.function.Supplier;

import org.jbpm.serverless.workflow.ObjectMapperSupplier;
import org.jbpm.serverless.workflow.actions.InjectAction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;

public class InjectActionSupplier extends InjectAction implements Supplier<Expression> {

    public InjectActionSupplier(JsonNode node) {
        super(node);
    }

    @Override
    public Expression get() {
        try {
            return new ObjectCreationExpr()
                    .setType(InjectAction.class.getCanonicalName())
                    .addArgument(new StringLiteralExpr(ObjectMapperSupplier.get().writeValueAsString(node).replace("\"",
                            "\\\"")));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }
}
