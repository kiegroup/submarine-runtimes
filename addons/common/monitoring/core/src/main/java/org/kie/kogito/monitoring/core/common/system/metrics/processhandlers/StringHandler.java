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

package org.kie.kogito.monitoring.core.common.system.metrics.processhandlers;

import java.util.Arrays;
import java.util.List;

import org.kie.kogito.KogitoGAV;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;

public class StringHandler implements TypeHandler<String> {

    private final String processType;
    private final KogitoGAV gav;
    private final MeterRegistry meterRegistry;

    public StringHandler(String processType, KogitoGAV gav, MeterRegistry meterRegistry) {
        this.processType = processType;
        this.gav = gav;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void record(String processId, String variableName, String sample) {
        getCounter(processId, variableName, sample).increment();
    }

    @Override
    public String getProcessType() {
        return processType;
    }

    private Counter getCounter(String processId, String variableName, String identifier) {
        return Counter
                .builder(processType + ProcessConstants.PROCESS_NAME_SUFFIX)
                .description(ProcessConstants.PROCESS_HELP)
                .tags(getTags(processId, variableName, identifier))
                .register(meterRegistry);
    }

    private List<Tag> getTags(String processId, String variableName, String identifier) {
        return Arrays.asList(
                Tag.of("artifactId", gav.getArtifactId()),
                Tag.of("version", gav.getVersion()),
                Tag.of("process_id", processId),
                Tag.of("variable_name", variableName),
                Tag.of("identifier", identifier));
    }
}
