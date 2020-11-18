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

package org.kie.kogito.lra.process;

import java.util.HashMap;
import java.util.Map;

import org.kie.kogito.Model;

public class LRAProcessModel implements Model {

    private String id;
    private Map<String, Object> params = new HashMap<>();


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Object get(String param) {
        return this.params.get(param);
    }

    @Override
    public void update(Map<String, Object> params) {
        params.entrySet().stream().forEach(e -> this.params.put(e.getKey(), e.getValue()));
    }

    public void fromMap(String id, Map<String, Object> params) {
        this.id = id;
        fromMap(params);
    }

    @Override
    public void fromMap(Map<String, Object> params) {
        this.params = params;
    }

    @Override
    public Map<String, Object> toMap() {
        return params;
    }
}
