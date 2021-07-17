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
package org.kie.kogito.incubation.processes.services.workitems;

import java.util.Optional;

import org.kie.kogito.MapOutput;
import org.kie.kogito.Model;
import org.kie.kogito.incubation.processes.workitem.WorkItemId;
import org.kie.kogito.process.WorkItem;
import org.kie.kogito.process.workitem.Policy;
import org.kie.kogito.process.workitem.Transition;

public interface WorkItemService {

    Optional<WorkItem> get(WorkItemId workItemId, Policy<?>... policies);

    Optional<Model> transition(WorkItemId workItemId, Transition<?> transition);

    Optional<Model> abort(WorkItemId workItemId, Transition<?> transition);

    Optional<Model> complete(WorkItemId workItemId, Transition<?> transition);

    Optional<Model> save(WorkItemId workItemId, MapOutput model, Policy<?>... policies);
}
