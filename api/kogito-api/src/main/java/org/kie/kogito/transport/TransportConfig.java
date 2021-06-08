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

package org.kie.kogito.transport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TransportConfig {

    public static final String TRANSPORT_CONTEXT = "transportContext";

    private final Collection<TransportFilter> filters;

    public TransportConfig() {
        this.filters = new ArrayList<>();
    }

    public TransportConfig(Iterable<TransportFilter> filters) {
        this.filters = new ArrayList<>();
        if (filters != null) {
            filters.forEach(this.filters::add);
        }
    }

    public boolean accepts(String paramName) {
        if (paramName == null || paramName.trim().isEmpty()) {
            return false;
        }
        return this.filters.stream().anyMatch(c -> c.accepts(paramName));
    }

    public Map<String, String> buildContext(Map<String, List<String>> params) {
        return params.entrySet()
                .stream()
                .filter(e -> accepts(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, e -> String.join(",", e.getValue())));
    }
}
