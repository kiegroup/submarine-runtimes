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

package org.kie.kogito.grafana.process;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.kie.kogito.grafana.model.functions.GrafanaFunction;
import org.kie.kogito.grafana.model.panel.common.YAxis;

public class SupportedProcessTypes {

    private static final Set<AbstractProcessType> supportedProcessTypes = new HashSet<>();

    private static final Map<Class, String> processInternalClassToProcessStandardMap = new HashMap<>();

    static {
        supportedProcessTypes.add(new StringType());
        supportedProcessTypes.forEach(x -> processInternalClassToProcessStandardMap.put(x.getInternalClass(), x.getProcessType()));
    }

    private SupportedProcessTypes() {
    }

    public static boolean isSupported(String type) {
        return processInternalClassToProcessStandardMap.containsValue(type);
    }

    public static boolean isSupported(Class c) {
        return processInternalClassToProcessStandardMap.containsKey(c);
    }

    public static String fromInternalToStandard(Class c) {
        return processInternalClassToProcessStandardMap.get(c);
    }

    public static Optional<GrafanaFunction> getGrafanaFunction(String processType) {
        if (isSupported(processType)) {
            Optional<AbstractProcessType> type = supportedProcessTypes.stream().filter(x -> x.getProcessType().equalsIgnoreCase(processType)).findFirst();
            if (type.isPresent()) {
                return Optional.ofNullable(type.get().getGrafanaFunction());
            }
        }
        return Optional.empty();
    }

    public static List<YAxis> getYAxis(String processType) {
        if (isSupported(processType)) {
            Optional<AbstractProcessType> type = supportedProcessTypes.stream().filter(x -> x.getProcessType().equalsIgnoreCase(processType)).findFirst();
            if (type.isPresent()) {
                return type.get().getYaxes();
            }
        }
        return new ArrayList<>();
    }

    public static Collection<String> getSupportedProcessTypes() {
        return processInternalClassToProcessStandardMap.values();
    }
}
