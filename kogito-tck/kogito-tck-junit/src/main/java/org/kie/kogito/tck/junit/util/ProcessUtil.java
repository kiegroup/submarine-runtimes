/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.kie.kogito.tck.junit.util;


import java.util.List;
import java.util.Map;

import org.kie.kogito.Application;
import org.kie.kogito.Model;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.Processes;
import org.kie.kogito.process.WorkItem;
import org.kie.kogito.tck.junit.api.KogitoUnitTestContext;

public final class ProcessUtil {

    public static void completeFirstPendingWorkItem(ProcessInstance<? extends Model> instance, Map<String, Object> outcome) {
        completeWorkItem(instance, 0, outcome);
    }

    public static void completeWorkItem(ProcessInstance<? extends Model> instance, int idx, Map<String, Object> outcome) {
        List<WorkItem> workItems = instance.workItems();
        WorkItem workItem = workItems.get(idx);
        instance.completeWorkItem(workItem.getId(), outcome);
    }
    
    public static ProcessInstance<? extends Model> startProcess(KogitoUnitTestContext context, String processId, Map<String, Object> params) {
        Processes processes = context.find(Application.class).get(Processes.class);
        Process<? extends Model> process = processes.processById(processId);
        Model model = process.createModel();
        model.update(params);
        ProcessInstance<? extends Model> instance = process.createInstance(model);
        instance.start();
        return instance;
    }

    public static ProcessInstance<? extends Model> startProcess(KogitoUnitTestContext context, String processId, String businessKey) {
        Processes processes = context.find(Application.class).get(Processes.class);
        Process<? extends Model> process = processes.processById(processId);
        Model model = process.createModel();
        ProcessInstance<? extends Model> instance = process.createInstance(businessKey, model);
        instance.start();
        return instance;
    }

    public static ProcessInstance<? extends Model> startProcess(KogitoUnitTestContext context, String processId) {
        Processes processes = context.find(Application.class).get(Processes.class);
        Process<? extends Model> process = processes.processById(processId);
        Model model = process.createModel();
        ProcessInstance<? extends Model> instance = process.createInstance(model);
        instance.start();
        return instance;
    }
}
