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

package org.kie.kogito.monitoring.core.common.system.metrics;

import java.util.HashMap;
import java.util.Map;

import org.kie.kogito.KogitoGAV;
import org.kie.kogito.Model;
import org.kie.kogito.grafana.process.SupportedProcessTypes;
import org.kie.kogito.monitoring.core.common.system.metrics.processhandlers.StringHandler;
import org.kie.kogito.monitoring.core.common.system.metrics.processhandlers.TypeHandler;

import io.micrometer.core.instrument.MeterRegistry;

public class ProcessDomainMetricsCollector {

    private final Map<Class, TypeHandler> handlers;

    public ProcessDomainMetricsCollector(KogitoGAV gav, MeterRegistry meterRegistry) {
        handlers = generateHandlers(gav, meterRegistry);
    }

    private static Map<Class, TypeHandler> generateHandlers(KogitoGAV gav, MeterRegistry meterRegistry) {
        HashMap<Class, TypeHandler> handlers = new HashMap<>();
        handlers.put(String.class, new StringHandler(SupportedProcessTypes.fromInternalToStandard(String.class), gav, meterRegistry));
        return handlers;
    }

    public void registerInput(Model inputModel, String processId) {
        if (inputModel == null) {
            return;
        }
        Map<String, Object> variables = inputModel.toMap();
        variables.forEach((varName, varObj) -> {
            if (varObj != null && SupportedProcessTypes.isSupported(varObj.getClass())) {
                handlers.get(varObj.getClass()).record(processId, varName, varObj);
            }
        });
    }

}
