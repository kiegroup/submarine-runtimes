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
package org.jbpm.process.instance.impl.humantask;

import java.util.Map;

import org.kie.kogito.MapInput;
import org.kie.kogito.Model;

class HumanTaskDataImpl implements Model {
    private Map<String, Object> map;

    public HumanTaskDataImpl(Map<String, Object> map) {
        this.map = map;
    }

    @Override
    public void update(Map<String, Object> params) {
        map.putAll(params);
    }

    @Override
    public MapInput fromMap(Map<String, Object> params) {
        map.putAll(params);
        return this;
    }

    @Override
    public Map<String, Object> toMap() {
        return map;
    }
}
