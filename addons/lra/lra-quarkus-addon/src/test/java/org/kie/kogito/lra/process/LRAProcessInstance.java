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

import java.util.Map;

import org.kie.api.runtime.process.ProcessRuntime;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.kie.kogito.process.impl.AbstractProcess;
import org.kie.kogito.process.impl.AbstractProcessInstance;

public class LRAProcessInstance extends AbstractProcessInstance<LRAProcessModel> {

    public LRAProcessInstance(AbstractProcess<LRAProcessModel> process, LRAProcessModel variables, ProcessRuntime rt) {
        super(process, variables, rt);
    }

    public LRAProcessInstance(AbstractProcess<LRAProcessModel> process, LRAProcessModel variables, String businessKey, ProcessRuntime rt) {
        super(process, variables, businessKey, rt);
    }

    public LRAProcessInstance(AbstractProcess<LRAProcessModel> process, LRAProcessModel variables, WorkflowProcessInstance wpi) {
        super(process, variables, wpi);
    }

    public LRAProcessInstance(AbstractProcess<LRAProcessModel> process, LRAProcessModel variables, ProcessRuntime rt, WorkflowProcessInstance wpi) {
        super(process, variables, rt, wpi);
    }

    @Override
    protected void unbind(LRAProcessModel variables, Map<String, Object> vmap) {
        variables.fromMap(this.id(), vmap);
    }

    @Override
    protected Map<String, Object> bind(LRAProcessModel variables) {
        return variables.toMap();
    }
}
