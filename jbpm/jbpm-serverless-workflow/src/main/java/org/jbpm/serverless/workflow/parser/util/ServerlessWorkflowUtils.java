/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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
package org.jbpm.serverless.workflow.parser.util;

import java.io.Reader;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.serverlessworkflow.api.Workflow;
import io.serverlessworkflow.api.branches.Branch;
import io.serverlessworkflow.api.events.EventDefinition;
import io.serverlessworkflow.api.functions.FunctionDefinition;
import io.serverlessworkflow.api.interfaces.State;
import io.serverlessworkflow.api.mapper.BaseObjectMapper;
import io.serverlessworkflow.api.mapper.JsonObjectMapper;
import io.serverlessworkflow.api.mapper.YamlObjectMapper;
import io.serverlessworkflow.api.states.DefaultState;
import io.serverlessworkflow.api.states.ParallelState;
import org.drools.core.util.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerlessWorkflowUtils {

    public static final String DEFAULT_WORKFLOW_FORMAT = "json";
    public static final String ALTERNATE_WORKFLOW_FORMAT = "yml";
    public static final String DEFAULT_JSONPATH_CONFIG = "com.jayway.jsonpath.Configuration jsonPathConfig = com.jayway.jsonpath.Configuration.builder()" +
            ".mappingProvider(new com.jayway.jsonpath.spi.mapper.JacksonMappingProvider())" +
            ".jsonProvider(new com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider()).build(); ";


    private static final String APP_PROPERTIES_BASE = "kogito.sw.";
    private static final String APP_PROPERTIES_FUNCTIONS_BASE = "functions.";
    private static final String APP_PROPERTIES_EVENTS_BASE = "events.";
    private static final String APP_PROPERTIES_STATES_BASE = "states.";

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerlessWorkflowUtils.class);

    private ServerlessWorkflowUtils() {
    }

    public static BaseObjectMapper getObjectMapper(String workflowFormat) {
        if (workflowFormat != null && workflowFormat.equalsIgnoreCase(DEFAULT_WORKFLOW_FORMAT)) {
            return new JsonObjectMapper();
        }

        if (workflowFormat != null && workflowFormat.equalsIgnoreCase(ALTERNATE_WORKFLOW_FORMAT)) {
            return new YamlObjectMapper();
        }

        LOGGER.error("unable to determine workflow format {}", workflowFormat);
        throw new IllegalArgumentException("invalid workflow format");
    }

    public static String readWorkflowFile(Reader reader) {
        return StringUtils.readFileAsString(reader);
    }

    public static State getWorkflowStartState(Workflow workflow) {
        return workflow.getStates().stream()
                .filter(ws -> ws.getStart() != null)
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Workflow does not have a Start state"));
    }

    public static List<State> getStatesByType(Workflow workflow, DefaultState.Type type) {
        return workflow.getStates().stream()
                .filter(ws -> ws.getType() == type)
                .collect(Collectors.toList());
    }

    public static List<State> getWorkflowEndStates(Workflow workflow) {
        return workflow.getStates().stream()
                .filter(ws -> ws.getEnd() != null)
                .collect(Collectors.toList());
    }

    public static boolean includesSupportedStates(Workflow workflow) {
        for (State state : workflow.getStates()) {
            if (!state.getType().equals(DefaultState.Type.EVENT)
                    && !state.getType().equals(DefaultState.Type.OPERATION)
                    && !state.getType().equals(DefaultState.Type.DELAY)
                    && !state.getType().equals(DefaultState.Type.SUBFLOW)
                    && !state.getType().equals(DefaultState.Type.INJECT)
                    && !state.getType().equals(DefaultState.Type.SWITCH)
                    && !state.getType().equals(DefaultState.Type.PARALLEL)) {
                return false;
            }

            if (state.getType().equals(DefaultState.Type.PARALLEL)) {
                if (!supportedParallelState((ParallelState) state)) {
                    LOGGER.warn("unsupported parallel state");
                    return false;
                }
            }

        }

        return true;
    }

    public static boolean supportedParallelState(ParallelState parallelState) {
        // currently support for only workflowId inside branches
        if (parallelState.getBranches() != null && parallelState.getBranches().size() > 0) {
            for (Branch branch : parallelState.getBranches()) {
                if(branch.getWorkflowId() == null || branch.getWorkflowId().length() < 1) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public static EventDefinition getWorkflowEventFor(Workflow workflow, String eventName) {
        return workflow.getEvents().getEventDefs().stream()
                .filter(wt -> wt.getName().equals(eventName))
                .findFirst().orElseThrow(() -> new NoSuchElementException("No event for " + eventName));
    }


    public static String sysOutFunctionScript(String script) {
        String retStr = DEFAULT_JSONPATH_CONFIG;
        retStr += "java.lang.String toPrint = \"\";com.fasterxml.jackson.databind.JsonNode jsonNode;";
        retStr += getJsonPathScript(script);
        retStr += "System.out.println(toPrint);";

        return retStr;
    }

    public static String scriptFunctionScript(String script) {
        String retStr = DEFAULT_JSONPATH_CONFIG;
        retStr += getJsonPathScript(script);
        return retStr;
    }

    public static String conditionScript(String conditionStr) {
        if (conditionStr.startsWith("{{")) {
            conditionStr = conditionStr.substring(2);
        }
        if (conditionStr.endsWith("}}")) {
            conditionStr = conditionStr.substring(0, conditionStr.length() - 2);
        }

        conditionStr = conditionStr.trim();

        // check if we are calling a different workflow var
        String processVar = "workflowdata";
        String otherVar = conditionStr.substring(conditionStr.indexOf("$") + 1, conditionStr.indexOf("."));

        if(otherVar.trim().length() > 0) {
            processVar = otherVar;
            conditionStr = conditionStr.replaceAll(otherVar, "");

        }

        return "return !((java.util.List<java.lang.String>) com.jayway.jsonpath.JsonPath.parse(((com.fasterxml.jackson.databind.JsonNode)kcontext.getVariable(\"" + processVar + "\")).toString()).read(\"" + conditionStr + "\")).isEmpty();";
    }

    public static String getJsonPathScript(String script) {
        if (script.contains("$")) {
            String replacement = "jsonNode = com.jayway.jsonpath.JsonPath.using(jsonPathConfig)" +
                    ".parse(((com.fasterxml.jackson.databind.JsonNode)kcontext.getVariable(\"workflowdata\")))" +
                    ".read(\"@@.$1\", com.fasterxml.jackson.databind.JsonNode.class); toPrint+= jsonNode.isTextual() ? jsonNode.asText() : jsonNode;";
            script = script.replaceAll("\\$.([A-Za-z]+)", replacement);
            script = script.replaceAll("@@", Matcher.quoteReplacement("$"));
        }
        return script;
    }

    public static String getInjectScript(JsonNode toInjectNode) {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String injectStr = objectMapper.writeValueAsString(toInjectNode);

            return "com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();\n" +
                    "        com.fasterxml.jackson.databind.JsonNode updateNode2 = objectMapper.readTree(\"" + injectStr.replaceAll("\"", "\\\\\"") + "\");\n" +
                    "        com.fasterxml.jackson.databind.JsonNode mainNode2 = (com.fasterxml.jackson.databind.JsonNode)kcontext.getVariable(\"workflowdata\");\n" +
                    "        java.util.Iterator<String> fieldNames2 = updateNode2.fieldNames();\n" +
                    "        while(fieldNames2.hasNext()) {\n" +
                    "            String updatedFieldName = fieldNames2.next();\n" +
                    "            com.fasterxml.jackson.databind.JsonNode updatedValue = updateNode2.get(updatedFieldName);\n" +
                    "            if(mainNode2.get(updatedFieldName) != null) {\n" +
                    "                ((com.fasterxml.jackson.databind.node.ObjectNode) mainNode2).replace(updatedFieldName, updatedValue);\n" +
                    "            } else {\n" +
                    "                ((com.fasterxml.jackson.databind.node.ObjectNode) mainNode2).put(updatedFieldName, updatedValue);\n" +
                    "            }\n" +
                    "        }\n" +
                    "        kcontext.setVariable(\"workflowdata\", mainNode2);\n";

        } catch (JsonProcessingException e) {
            LOGGER.warn("unable to set inject script: {}", e.getMessage());
            return "";
        }
    }

    public static String resolveFunctionMetadata(FunctionDefinition function, String metadataKey, WorkflowAppContext workflowAppContext) {
        if (function != null && function.getMetadata() != null && function.getMetadata().containsKey(metadataKey)) {
            return function.getMetadata().get(metadataKey);
        }

        if (function != null && workflowAppContext != null &&
                workflowAppContext.getApplicationProperties().containsKey(APP_PROPERTIES_BASE + APP_PROPERTIES_FUNCTIONS_BASE + function.getName() + "." + metadataKey)) {
            return workflowAppContext.getApplicationProperty(APP_PROPERTIES_BASE + APP_PROPERTIES_FUNCTIONS_BASE + function.getName() + "." + metadataKey);
        }

        LOGGER.warn("Could not resolve function metadata: {}", metadataKey);
        return "";
    }

    public static String resolveEvenDefinitiontMetadata(EventDefinition eventDefinition, String metadataKey, WorkflowAppContext workflowAppContext) {
        if (eventDefinition != null && eventDefinition.getMetadata() != null && eventDefinition.getMetadata().containsKey(metadataKey)) {
            return eventDefinition.getMetadata().get(metadataKey);
        }

        if (eventDefinition != null && workflowAppContext != null &&
                workflowAppContext.getApplicationProperties().containsKey(APP_PROPERTIES_BASE + APP_PROPERTIES_EVENTS_BASE + eventDefinition.getName() + "." + metadataKey)) {
            return workflowAppContext.getApplicationProperty(APP_PROPERTIES_BASE + APP_PROPERTIES_EVENTS_BASE + eventDefinition.getName() + "." + metadataKey);
        }

        LOGGER.warn("Could not resolve event definition metadata: {}", metadataKey);
        return "";
    }

    public static String resolveStatetMetadata(State state, String metadataKey, WorkflowAppContext workflowAppContext) {
        if (state != null && state.getMetadata() != null && state.getMetadata().containsKey(metadataKey)) {
            return state.getMetadata().get(metadataKey);
        }

        if (state != null && workflowAppContext != null &&
                workflowAppContext.getApplicationProperties().containsKey(APP_PROPERTIES_BASE + APP_PROPERTIES_STATES_BASE + state.getName() + "." + metadataKey)) {
            return workflowAppContext.getApplicationProperty(APP_PROPERTIES_BASE + APP_PROPERTIES_STATES_BASE + state.getName() + "." + metadataKey);
        }

        LOGGER.warn("Could not resolve state metadata: {}", metadataKey);
        return "";
    }

    public static String resolveWorkflowMetadata(Workflow workflow, String metadataKey, WorkflowAppContext workflowAppContext) {
        if (workflow != null && workflow.getMetadata() != null && workflow.getMetadata().containsKey(metadataKey)) {
            return workflow.getMetadata().get(metadataKey);
        }

        if (workflow != null && workflowAppContext != null &&
                workflowAppContext.getApplicationProperties().containsKey(APP_PROPERTIES_BASE + workflow.getId() + "." + metadataKey)) {
            return workflowAppContext.getApplicationProperty(APP_PROPERTIES_BASE + workflow.getId() + "." + metadataKey);
        }

        LOGGER.warn("Could not resolve state metadata: {}", metadataKey);
        return "";
    }


}
