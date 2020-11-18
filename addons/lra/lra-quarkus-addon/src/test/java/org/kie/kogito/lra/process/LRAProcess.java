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

import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import io.narayana.lra.client.NarayanaLRAClient;
import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;
import org.jbpm.process.core.datatype.impl.type.ObjectDataType;
import org.jbpm.process.core.datatype.impl.type.StringDataType;
import org.jbpm.process.core.event.EventTypeFilter;
import org.jbpm.ruleflow.core.RuleFlowProcessFactory;
import org.kie.api.definition.process.Process;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.kie.kogito.Model;
import org.kie.kogito.lra.KogitoLRA;
import org.kie.kogito.lra.listeners.LRAEventListenerConfig;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.impl.AbstractProcess;
import org.kie.kogito.process.impl.DefaultWorkItemHandlerConfig;
import org.kie.kogito.process.impl.StaticProcessConfig;
import org.kie.kogito.services.uow.CollectingUnitOfWorkFactory;
import org.kie.kogito.services.uow.DefaultUnitOfWorkManager;

import static org.kie.kogito.lra.KogitoLRA.METADATA_TIMEOUT;
import static org.kie.kogito.lra.KogitoLRA.METADATA_TYPE;

@ApplicationScoped
@Named("lra-process")
public class LRAProcess extends AbstractProcess<LRAProcessModel> {

    public static final Long LRA_TIMEOUT = 1000L;

    private LRA.Type lraType;

    @Inject
    protected LRAProcess(@Named(KogitoLRA.BEAN_NAME) NarayanaLRAClient lraClient) {
        super(new StaticProcessConfig(new DefaultWorkItemHandlerConfig(), new LRAEventListenerConfig(lraClient), new DefaultUnitOfWorkManager(new CollectingUnitOfWorkFactory()), null));
    }

    public void setLraType(LRA.Type lraType) {
        this.lraType = lraType;
    }

    @Override
    public ProcessInstance<LRAProcessModel> createInstance(WorkflowProcessInstance wpi) {
        return new LRAProcessInstance(this, this.createModel(), this.createProcessRuntime(), wpi);
    }

    @Override
    public ProcessInstance<LRAProcessModel> createReadOnlyInstance(WorkflowProcessInstance wpi) {
        return new LRAProcessInstance(this, this.createModel(), wpi);
    }

    @Override
    public LRAProcessModel createModel() {
        return new LRAProcessModel();
    }

    @Override
    public Process process() {
        EventTypeFilter lraCompensateFilter = new EventTypeFilter();
        lraCompensateFilter.setType("LRA-compensate");
        RuleFlowProcessFactory factory = RuleFlowProcessFactory.createProcess("lra-process");
        if(lraType != null) {
            factory.metaData(METADATA_TYPE, lraType.name());
        }
        factory.variable("history", new ObjectDataType(List.class.getName()))
                .variable("message", new StringDataType())
                .name("LRA")
                .metaData(METADATA_TIMEOUT, LRA_TIMEOUT)
                .packageName("org.kie.kogito.lra.test")
                .startNode(1).name("Start").done()
                .humanTaskNode(2)
                .name("Human Task")
                .outMapping("message", "message")
                .done()
                .actionNode(3)
                .name("Script Task")
                .action(kcontext -> {
                    List<String> history = (List<String>) kcontext.getVariable("history");
                    String message = (String) kcontext.getVariable("message");
                    if (message.equalsIgnoreCase("fail")) {
                        throw new RuntimeException("Failing on purpose");
                    }
                    history.add(message);
                    System.out.println("Script task");
                })
                .done()
                .endNode(4)
                .name("End")
                .done()
                .connection(1, 2)
                .connection(2, 3)
                .connection(3, 4);
        return factory.validate().getProcess();
    }

    @Override
    public ProcessInstance<LRAProcessModel> createInstance(LRAProcessModel value) {
        return new LRAProcessInstance(this, value, this.createProcessRuntime());
    }

    @Override
    public ProcessInstance<LRAProcessModel> createInstance(String businessKey, LRAProcessModel value) {
        return new LRAProcessInstance(this, value, businessKey, this.createProcessRuntime());
    }

    @Override
    public ProcessInstance<? extends Model> createInstance(Model m) {
        return createInstance((LRAProcessModel) m);
    }
}
