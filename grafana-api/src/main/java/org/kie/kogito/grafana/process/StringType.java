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

import org.kie.kogito.grafana.model.functions.BaseExpression;
import org.kie.kogito.grafana.model.functions.IncreaseFunction;
import org.kie.kogito.grafana.model.functions.SumByFunction;

public class StringType extends AbstractProcessType {
    private static final String PROCESS_TYPE = "StringDataType";
    private static final String NAME_SUFFIX = "total";

    public StringType() {
        super(String.class, PROCESS_TYPE, NAME_SUFFIX);

        BaseExpression baseExpression = new BaseExpression(PROCESS_TYPE, NAME_SUFFIX);
        setGrafanaFunction(new SumByFunction(new IncreaseFunction(baseExpression, "1m"), "identifier"));
    }
}
