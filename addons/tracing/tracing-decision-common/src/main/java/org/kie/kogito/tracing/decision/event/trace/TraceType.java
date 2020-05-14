/*
 *  Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.kie.kogito.tracing.decision.event.trace;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.kie.dmn.api.core.DMNType;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonInclude(NON_NULL)
public class TraceType {
    private final String id;
    private final String namespace;
    private final String name;

    public TraceType(String id, String namespace, String name) {
        this.id = id;
        this.namespace = namespace;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getName() {
        return name;
    }

    public static TraceType from(DMNType dmnType) {
        return new TraceType(dmnType.getId(), dmnType.getNamespace(), dmnType.getName());
    }
}
