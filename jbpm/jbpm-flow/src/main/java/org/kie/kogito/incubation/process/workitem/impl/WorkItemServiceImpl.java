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
package org.kie.kogito.incubation.process.workitem.impl;

import java.util.Optional;

import org.jbpm.process.instance.impl.humantask.HumanTaskHelper;
import org.kie.kogito.Application;
import org.kie.kogito.MapOutput;
import org.kie.kogito.Model;
import org.kie.kogito.incubation.processes.services.workitems.WorkItemService;
import org.kie.kogito.incubation.processes.workitem.WorkItemId;
import org.kie.kogito.process.ProcessInstanceReadMode;
import org.kie.kogito.process.Processes;
import org.kie.kogito.process.WorkItem;
import org.kie.kogito.process.workitem.Policy;
import org.kie.kogito.process.workitem.Transition;
import org.kie.kogito.services.uow.UnitOfWorkExecutor;

public class WorkItemServiceImpl implements WorkItemService {
    final Application application;
    private final Processes processes;

    public WorkItemServiceImpl(Application application) {
        this.application = application;
        this.processes = application.get(Processes.class);
    }

    public Optional<WorkItem> get(WorkItemId workItemId, Policy<?>... policies) {
        return processes.processById(workItemId.processInstanceId().processId().processId())
                .instances()
                .findById(workItemId.processInstanceId().processInstanceId(), ProcessInstanceReadMode.READ_ONLY)
                .map(pi -> pi.workItem(workItemId.workItemId(), policies));

    }

    public Optional<Model> transition(WorkItemId workItemId, Transition<?> transition) {
        return UnitOfWorkExecutor.executeInUnitOfWork(
                application.unitOfWorkManager(), () -> processes.processById(workItemId.processInstanceId().processId().processId())
                        .instances()
                        .findById(workItemId.processInstanceId().processInstanceId())
                        .map(pi -> {
                            pi.transitionWorkItem(workItemId.workItemId(), transition);
                            return pi.variables();
                        }));

    }

    @Override
    public Optional<Model> abort(WorkItemId workItemId, Transition<?> transition) {
        return UnitOfWorkExecutor.executeInUnitOfWork(application.unitOfWorkManager(), () -> processes.processById(workItemId.processInstanceId().processId().processId())
                .instances()
                .findById(workItemId.processInstanceId().processInstanceId())
                .map(pi -> {
                    pi.transitionWorkItem(workItemId.workItemId(), transition);
                    return pi.variables();
                }));
    }

    @Override
    public Optional<Model> complete(WorkItemId workItemId, Transition<?> transition) {
        return UnitOfWorkExecutor.executeInUnitOfWork(application.unitOfWorkManager(), () -> processes.processById(workItemId.processInstanceId().processId().processId())
                .instances()
                .findById(workItemId.processInstanceId().processInstanceId())
                .map(pi -> {
                    pi.transitionWorkItem(workItemId.workItemId(), transition);
                    return pi.variables();
                }));
    }

    @Override
    public Optional<Model> save(WorkItemId workItemId, MapOutput model, Policy<?>... policies) {
        return UnitOfWorkExecutor.executeInUnitOfWork(
                application.unitOfWorkManager(), () -> processes.processById(workItemId.processInstanceId().processId().processId())
                        .instances()
                        .findById(workItemId.processInstanceId().processInstanceId())
                        .map(pi -> pi.updateWorkItem(workItemId.workItemId(), wi -> HumanTaskHelper.updateContent(wi, model), policies)));
    }
}
