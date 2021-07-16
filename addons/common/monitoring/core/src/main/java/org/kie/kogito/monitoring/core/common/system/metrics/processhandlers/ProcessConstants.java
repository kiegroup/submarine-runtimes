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

import java.time.Duration;

public class ProcessConstants {

    public static final String PROCESS_NAME_SUFFIX = "_process_input";
    public static final String PROCESS_HELP = "Process input.";
    public static final double[] SUMMARY_PERCENTILES = new double[] { 0.1, 0.25, 0.5, 0.75, 0.9, 0.99 };
    public static final Duration SUMMARY_EXPIRATION = Duration.ofMinutes(3);

    /**
     * Array of label names for a prometheus object that needs an handler and an identifier.
     */
    public static final String[] PROCESS_ENDPOINT_IDENTIFIER_LABELS = new String[] { "process_id", "variable_name", "identifier", "artifactId", "version" };
    /**
     * Array of label names for a prometheus object that needs only the handler.
     */
    public static final String[] PROCESS_ENDPOINT_LABELS = new String[] { "process_id", "variable_name" };

    private ProcessConstants() {
    }
}
