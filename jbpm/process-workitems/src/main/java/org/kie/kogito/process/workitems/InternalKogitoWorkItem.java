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
package org.kie.kogito.process.workitems;

import java.util.Date;
import java.util.Map;

import org.kie.kogito.internal.process.runtime.KogitoNodeInstance;
import org.kie.kogito.internal.process.runtime.KogitoProcessInstance;

public interface InternalKogitoWorkItem extends org.kie.kogito.internal.process.runtime.KogitoWorkItem {

    void setProcessInstanceId(String processInstanceId);

    void setNodeInstanceId(String deploymentId);

    String getNodeInstanceStringId();

    void setPhaseId(String phaseId);

    void setPhaseStatus(String phaseStatus);

    void setStartDate(Date date);

    void setCompleteDate(Date date);

    void setNodeInstance(KogitoNodeInstance nodeInstance);

    void setProcessInstance(KogitoProcessInstance processInstance);

    void setParameters(Map<String, Object> map);

    void setResults(Map<String, Object> results);

    void setState(int state);

    void setName(String name);

    void setProcessInstanceId(long processInstanceId);

    void setParameter(String name, Object value);

    String getDeploymentId();

    void setDeploymentId(String deploymentId);

    void setNodeInstanceId(long deploymentId);

    long getNodeInstanceId();

    long getNodeId();

    void setNodeId(long nodeId);
}
