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
package org.jbpm.serverless.workflow.parser.handlers;

import org.jbpm.ruleflow.core.RuleFlowNodeContainerFactory;
import org.jbpm.ruleflow.core.factory.ActionNodeFactory;
import org.jbpm.serverless.workflow.actions.InjectAction;
import org.jbpm.serverless.workflow.parser.NodeIdGenerator;

import com.fasterxml.jackson.databind.JsonNode;

import io.serverlessworkflow.api.Workflow;
import io.serverlessworkflow.api.states.InjectState;

public class InjectHandler<P extends RuleFlowNodeContainerFactory<P, ?>> extends StateHandler<InjectState, ActionNodeFactory<P>, P> {

    protected InjectHandler(InjectState state, Workflow workflow, RuleFlowNodeContainerFactory<P, ?> factory,
            NodeIdGenerator idGenerator) {
        super(state, workflow, factory, idGenerator);
    }

    @Override
    public ActionNodeFactory<P> makeNode() {
        ActionNodeFactory<P> actionNodeFactory = factory.actionNode(idGenerator.getId()).name(
                state.getName());
        JsonNode node = state.getData();
        if (node != null) {
            actionNodeFactory.action(new InjectAction(node));
        }
        return actionNodeFactory;
    }

}
