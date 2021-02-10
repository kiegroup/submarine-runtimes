/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.bpmn2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.bpmn2.handler.ServiceTaskHandler;
import org.jbpm.bpmn2.handler.SignallingTaskHandlerDecorator;
import org.jbpm.bpmn2.objects.ExceptionOnPurposeHandler;
import org.jbpm.bpmn2.objects.MyError;
import org.jbpm.bpmn2.objects.Person;
import org.jbpm.bpmn2.objects.TestWorkItemHandler;
import org.jbpm.process.instance.event.listeners.RuleAwareProcessEventListener;
import org.jbpm.process.instance.impl.demo.DoNothingWorkItemHandler;
import org.jbpm.process.instance.impl.demo.SystemOutWorkItemHandler;
import org.jbpm.workflow.instance.WorkflowProcessInstance;
import org.junit.jupiter.api.Test;
import org.kie.api.KieBase;
import org.kie.api.event.process.DefaultProcessEventListener;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.process.ProcessNodeLeftEvent;
import org.kie.api.event.process.ProcessNodeTriggeredEvent;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.kogito.internal.process.runtime.KogitoNodeInstance;
import org.kie.kogito.internal.process.runtime.KogitoProcessInstance;
import org.kie.kogito.internal.process.runtime.KogitoProcessRuntime;
import org.kie.kogito.internal.process.runtime.KogitoWorkItem;
import org.kie.kogito.internal.process.runtime.KogitoWorkItemManager;
import org.kie.kogito.process.workitem.WorkItemExecutionError;

public class ErrorEventTest extends JbpmBpmn2TestCase {

    private ProcessEventListener LOGGING_EVENT_LISTENER = new DefaultProcessEventListener() {

        @Override
        public void afterNodeLeft(ProcessNodeLeftEvent event) {
            logger.info("After node left {}", event.getNodeInstance().getNodeName());
        }

        @Override
        public void afterNodeTriggered(ProcessNodeTriggeredEvent event) {
            logger.info("After node triggered {}", event.getNodeInstance().getNodeName());
        }

        @Override
        public void beforeNodeLeft(ProcessNodeLeftEvent event) {
            logger.info("Before node left {}", event.getNodeInstance().getNodeName());
        }

        @Override
        public void beforeNodeTriggered(ProcessNodeTriggeredEvent event) {
            logger.info("Before node triggered {}", event.getNodeInstance().getNodeName());
        }

    };

    @Test
    public void testEventSubprocessError() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-EventSubprocessError.bpmn2");
        final List<String> executednodes = new ArrayList<>();
        ProcessEventListener listener = new DefaultProcessEventListener() {

            @Override
            public void afterNodeLeft(ProcessNodeLeftEvent event) {
                if (event.getNodeInstance().getNodeName()
                        .equals("Script Task 1")) {
                    executednodes.add(((KogitoNodeInstance) event.getNodeInstance()).getStringId());
                }
            }

        };
        ksession = createKnowledgeSession(kbase);
        KogitoProcessRuntime kruntime = KogitoProcessRuntime.asKogitoProcessRuntime(ksession);

        ksession.addEventListener(listener);
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        kruntime.getWorkItemManager().registerWorkItemHandler("Human Task", workItemHandler);
        KogitoProcessInstance processInstance = kruntime.startProcess("BPMN2-EventSubprocessError");
        assertProcessInstanceActive(processInstance);
        ksession = restoreSession(ksession, true);
        ksession.addEventListener(listener);

        KogitoWorkItem workItem = workItemHandler.getWorkItem();
        assertNotNull(workItem);
        kruntime.getWorkItemManager().completeWorkItem(workItem.getStringId(), null);
        assertProcessInstanceFinished(processInstance, ksession);
        assertNodeTriggered(processInstance.getStringId(), "start", "User Task 1",
                "end", "Sub Process 1", "start-sub", "Script Task 1", "end-sub");
        assertEquals(1, executednodes.size());

    }

    @Test
    public void testEventSubprocessErrorThrowOnTask() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-EventSubprocessError.bpmn2");
        final List<String> executednodes = new ArrayList<>();
        ProcessEventListener listener = new DefaultProcessEventListener() {

            @Override
            public void afterNodeLeft(ProcessNodeLeftEvent event) {
                if (event.getNodeInstance().getNodeName()
                        .equals("Script Task 1")) {
                    executednodes.add(((KogitoNodeInstance) event.getNodeInstance()).getStringId());
                }
            }

        };
        ksession = createKnowledgeSession(kbase);
        ksession.addEventListener(listener);
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", new TestWorkItemHandler() {

            @Override
            public void executeWorkItem(KogitoWorkItem workItem, KogitoWorkItemManager manager) {
                throw new MyError();

            }

            @Override
            public void abortWorkItem(KogitoWorkItem workItem, KogitoWorkItemManager manager) {
                manager.abortWorkItem(workItem.getStringId());
            }

        });

        KogitoProcessRuntime kruntime = KogitoProcessRuntime.asKogitoProcessRuntime(ksession);
        KogitoProcessInstance processInstance = kruntime.startProcess("BPMN2-EventSubprocessError");

        assertProcessInstanceFinished(processInstance, ksession);
        assertProcessInstanceAborted(processInstance);
        assertNodeTriggered(processInstance.getStringId(), "start", "User Task 1",
                "Sub Process 1", "start-sub", "Script Task 1", "end-sub");
        assertEquals(1, executednodes.size());

    }

    @Test
    public void testEventSubprocessErrorWithErrorCode() throws Exception {
        KieBase kbase = createKnowledgeBase("subprocess/EventSubprocessErrorHandlingWithErrorCode.bpmn2");
        final List<String> executednodes = new ArrayList<>();
        ProcessEventListener listener = new DefaultProcessEventListener() {

            @Override
            public void afterNodeLeft(ProcessNodeLeftEvent event) {
                if (event.getNodeInstance().getNodeName()
                        .equals("Script2")) {
                    executednodes.add(((KogitoNodeInstance) event.getNodeInstance()).getStringId());
                }
            }

        };
        ksession = createKnowledgeSession(kbase);
        ksession.addEventListener(listener);

        KogitoProcessRuntime kruntime = KogitoProcessRuntime.asKogitoProcessRuntime(ksession);
        KogitoProcessInstance processInstance = kruntime.startProcess("order-fulfillment-bpm.ccc");

        assertProcessInstanceFinished(processInstance, ksession);
        assertNodeTriggered(processInstance.getStringId(), "start", "Script1", "starterror", "Script2", "end2",
                "eventsubprocess");
        assertProcessVarValue(processInstance, "CapturedException", "java.lang.RuntimeException: XXX");
        assertEquals(1, executednodes.size());

    }

    @Test
    public void testEventSubprocessErrorWithOutErrorCode() throws Exception {
        KieBase kbase = createKnowledgeBaseWithoutDumper("subprocess/EventSubprocessErrorHandlingWithOutErrorCode.bpmn2");
        final List<String> executednodes = new ArrayList<>();
        ProcessEventListener listener = new DefaultProcessEventListener() {

            @Override
            public void afterNodeLeft(ProcessNodeLeftEvent event) {
                if (event.getNodeInstance().getNodeName()
                        .equals("Script2")) {
                    executednodes.add(((KogitoNodeInstance) event.getNodeInstance()).getStringId());
                }
            }

        };
        ksession = createKnowledgeSession(kbase);
        ksession.addEventListener(listener);

        KogitoProcessRuntime kruntime = KogitoProcessRuntime.asKogitoProcessRuntime(ksession);
        KogitoProcessInstance processInstance = kruntime.startProcess("order-fulfillment-bpm.ccc");

        assertProcessInstanceFinished(processInstance, ksession);
        assertNodeTriggered(processInstance.getStringId(), "start", "Script1", "starterror", "Script2", "end2",
                "eventsubprocess");
        assertProcessVarValue(processInstance, "CapturedException", "java.lang.RuntimeException: XXX");
        assertEquals(1, executednodes.size());

    }

    @Test
    public void testErrorBoundaryEvent() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-ErrorBoundaryEventInterrupting.bpmn2");
        ksession = createKnowledgeSession(kbase);
        ksession.getWorkItemManager().registerWorkItemHandler("MyTask",
                new DoNothingWorkItemHandler());
        ProcessInstance processInstance = ksession
                .startProcess("ErrorBoundaryEvent");
        assertProcessInstanceFinished(processInstance, ksession);

    }

    @Test
    public void testErrorBoundaryEventOnTask() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-ErrorBoundaryEventOnTask.bpmn2");
        ksession = createKnowledgeSession(kbase);
        TestWorkItemHandler handler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", handler);

        KogitoProcessRuntime kruntime = KogitoProcessRuntime.asKogitoProcessRuntime(ksession);
        KogitoProcessInstance processInstance = kruntime.startProcess("BPMN2-ErrorBoundaryEventOnTask");

        List<KogitoWorkItem> workItems = handler.getWorkItems();
        assertEquals(2, workItems.size());

        KogitoWorkItem workItem = workItems.get(0);
        if (!"john".equalsIgnoreCase((String) workItem.getParameter("ActorId"))) {
            workItem = workItems.get(1);
        }

        kruntime.getWorkItemManager().completeWorkItem(workItem.getStringId(), null);
        assertProcessInstanceFinished(processInstance, ksession);
        assertProcessInstanceAborted(processInstance);
        assertNodeTriggered(processInstance.getStringId(), "start", "split", "User Task", "User task error attached",
                "error end event");
        assertNotNodeTriggered(processInstance.getStringId(), "Script Task", "error1", "error2");
    }

    @Test
    public void testErrorBoundaryEventOnServiceTask() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-ErrorBoundaryEventOnServiceTask.bpmn2");
        ksession = createKnowledgeSession(kbase);
        TestWorkItemHandler handler = new TestWorkItemHandler();

        KogitoProcessRuntime kruntime = KogitoProcessRuntime.asKogitoProcessRuntime(ksession);
        kruntime.getWorkItemManager().registerWorkItemHandler("Human Task", handler);
        kruntime.getWorkItemManager().registerWorkItemHandler("Service Task", new ServiceTaskHandler());

        Map<String, Object> params = new HashMap<>();
        params.put("s", "test");
        KogitoProcessInstance processInstance = kruntime.startProcess("BPMN2-ErrorBoundaryEventOnServiceTask", params);

        List<KogitoWorkItem> workItems = handler.getWorkItems();
        assertEquals(1, workItems.size());
        kruntime.getWorkItemManager().completeWorkItem(workItems.get(0).getStringId(), null);

        assertProcessInstanceFinished(processInstance, ksession);
        assertNodeTriggered(processInstance.getStringId(), "start", "split", "User Task", "Service task error attached", "end0",
                "Script Task", "error2");

        assertNotNodeTriggered(processInstance.getStringId(), "end");
    }

    @Test
    public void testErrorBoundaryEventOnBusinessRuleTask() throws Exception {
        KieBase kbase = createKnowledgeBaseWithoutDumper("BPMN2-ErrorBoundaryEventOnBusinessRuleTask.bpmn2",
                "BPMN2-ErrorBoundaryEventOnBusinessRuleTask.drl");
        ksession = createKnowledgeSession(kbase);
        ksession.addEventListener(new RuleAwareProcessEventListener());

        KogitoProcessRuntime kruntime = KogitoProcessRuntime.asKogitoProcessRuntime(ksession);
        KogitoProcessInstance processInstance = kruntime.startProcess("BPMN2-ErrorBoundaryEventOnBusinessRuleTask");

        assertProcessInstanceFinished(processInstance, ksession);
        assertNodeTriggered(processInstance.getStringId(), "start", "business rule task error attached", "error1");
    }

    @Test
    public void testMultiErrorBoundaryEventsOnBusinessRuleTask() throws Exception {
        KieBase kbase = createKnowledgeBaseWithoutDumper("BPMN2-MultiErrorBoundaryEventsOnBusinessRuleTask.bpmn2",
                "BPMN2-MultiErrorBoundaryEventsOnBusinessRuleTask.drl");
        ksession = createKnowledgeSession(kbase);
        ksession.addEventListener(new RuleAwareProcessEventListener());

        Map<String, Object> params = new HashMap<>();
        params.put("person", new Person());

        KogitoProcessRuntime kruntime = KogitoProcessRuntime.asKogitoProcessRuntime(ksession);
        KogitoProcessInstance processInstance =
                kruntime.startProcess("BPMN2-MultiErrorBoundaryEventeOnBusinessRuleTask", params);

        assertProcessInstanceFinished(processInstance, ksession);
        assertNodeTriggered(processInstance.getStringId(), "start", "business rule task error attached",
                "NPE Script Task", "error1");

        ksession.dispose();

        ksession = createKnowledgeSession(kbase);
        kruntime = KogitoProcessRuntime.asKogitoProcessRuntime(ksession);

        ksession.addEventListener(new RuleAwareProcessEventListener());
        params = new HashMap<String, Object>();
        params.put("person", new Person("unsupported"));
        KogitoProcessInstance processInstance2 =
                kruntime.startProcess("BPMN2-MultiErrorBoundaryEventeOnBusinessRuleTask", params);
        assertProcessInstanceFinished(processInstance2, ksession);
        assertNodeTriggered(processInstance2.getStringId(), "start", "business rule task error attached",
                "UOE Script Task", "error2");
    }

    @Test
    public void testCatchErrorBoundaryEventOnTask() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-ErrorBoundaryEventOnTask.bpmn2");
        ksession = createKnowledgeSession(kbase);
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", new TestWorkItemHandler() {

            @Override
            public void executeWorkItem(KogitoWorkItem workItem, KogitoWorkItemManager manager) {
                if (workItem.getParameter("ActorId").equals("mary")) {
                    throw new MyError();
                }
            }

            @Override
            public void abortWorkItem(KogitoWorkItem workItem, KogitoWorkItemManager manager) {
                manager.abortWorkItem(workItem.getStringId());
            }

        });

        KogitoProcessRuntime kruntime = KogitoProcessRuntime.asKogitoProcessRuntime(ksession);
        KogitoProcessInstance processInstance = kruntime.startProcess("BPMN2-ErrorBoundaryEventOnTask");

        assertProcessInstanceActive(processInstance);
        assertNodeTriggered(processInstance.getStringId(), "start", "split", "User Task", "User task error attached",
                "Script Task", "error1", "error2");

    }

    @Test
    public void testErrorSignallingExceptionServiceTask() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-ExceptionServiceProcess-ErrorSignalling.bpmn2");
        ksession = createKnowledgeSession(kbase);

        StandaloneBPMNProcessTest.runTestErrorSignallingExceptionServiceTask(ksession);
    }

    @Test
    public void testSignallingExceptionServiceTask() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-ExceptionServiceProcess-Signalling.bpmn2");
        ksession = createKnowledgeSession(kbase);

        StandaloneBPMNProcessTest.runTestSignallingExceptionServiceTask(ksession);
    }

    @Test
    public void testEventSubProcessErrorWithScript() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-EventSubProcessErrorWithScript.bpmn2");
        ksession = createKnowledgeSession(kbase);
        ksession.getWorkItemManager().registerWorkItemHandler("Request Handler",
                new SignallingTaskHandlerDecorator(ExceptionOnPurposeHandler.class, "Error-90277"));
        ksession.getWorkItemManager().registerWorkItemHandler("Error Handler", new SystemOutWorkItemHandler());

        ProcessInstance processInstance = ksession.startProcess("com.sample.process");

        assertProcessInstanceAborted(processInstance);
        assertEquals("90277", ((WorkflowProcessInstance) processInstance).getOutcome());

    }

    @Test
    public void testErrorBoundaryEventOnEntry() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-BoundaryErrorEventCatchingOnEntryException.bpmn2");
        ksession = createKnowledgeSession(kbase);
        TestWorkItemHandler handler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", handler);

        KogitoProcessRuntime kruntime = KogitoProcessRuntime.asKogitoProcessRuntime(ksession);
        KogitoProcessInstance processInstance = kruntime.startProcess("BoundaryErrorEventOnEntry");

        assertProcessInstanceActive(processInstance.getStringId(), ksession);
        assertEquals(1, handler.getWorkItems().size());
    }

    @Test
    public void testErrorBoundaryEventOnExit() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-BoundaryErrorEventCatchingOnExitException.bpmn2");
        ksession = createKnowledgeSession(kbase);
        KogitoProcessRuntime kruntime = KogitoProcessRuntime.asKogitoProcessRuntime(ksession);
        TestWorkItemHandler handler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", handler);

        KogitoProcessInstance processInstance = kruntime.startProcess("BoundaryErrorEventOnExit");

        assertProcessInstanceActive(processInstance.getStringId(), ksession);
        KogitoWorkItem workItem = handler.getWorkItem();
        kruntime.getWorkItemManager().completeWorkItem(workItem.getStringId(), null);

        assertEquals(1, handler.getWorkItems().size());
    }

    @Test
    public void testBoundaryErrorEventDefaultHandlerWithErrorCodeWithStructureRef() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-BoundaryErrorEventDefaultHandlerWithErrorCodeWithStructureRef.bpmn2");
        ksession = createKnowledgeSession(kbase);
        ExceptionWorkItemHandler handler = new ExceptionWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", handler);

        KogitoProcessRuntime kruntime = KogitoProcessRuntime.asKogitoProcessRuntime(ksession);
        KogitoProcessInstance processInstance = kruntime.startProcess("com.sample.bpmn.hello");
        assertEquals(KogitoProcessInstance.STATE_ERROR, processInstance.getState());
    }

    @Test
    public void testBoundaryErrorEventDefaultHandlerWithWorkItemExecutionError() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-BoundaryErrorEventDefaultHandlerByErrorCode.bpmn2");
        ksession = createKnowledgeSession(kbase);
        WorkItemExecutionErrorWorkItemHandler handler = new WorkItemExecutionErrorWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", handler);

        KogitoProcessRuntime kruntime = KogitoProcessRuntime.asKogitoProcessRuntime(ksession);
        KogitoProcessInstance processInstance = kruntime.startProcess("com.sample.bpmn.hello");
        assertEquals(ProcessInstance.STATE_COMPLETED, processInstance.getState());
    }

    @Test
    public void testBoundaryErrorEventDefaultHandlerWithErrorCodeWithoutStructureRef() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-BoundaryErrorEventDefaultHandlerWithErrorCodeWithoutStructureRef.bpmn2");
        ksession = createKnowledgeSession(kbase);
        ExceptionWorkItemHandler handler = new ExceptionWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", handler);

        KogitoProcessRuntime kruntime = KogitoProcessRuntime.asKogitoProcessRuntime(ksession);
        KogitoProcessInstance processInstance = kruntime.startProcess("com.sample.bpmn.hello");
        assertEquals(KogitoProcessInstance.STATE_ERROR, processInstance.getState());
    }

    @Test
    public void testBoundaryErrorEventDefaultHandlerWithoutErrorCodeWithStructureRef() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-BoundaryErrorEventDefaultHandlerWithoutErrorCodeWithStructureRef.bpmn2");
        ksession = createKnowledgeSession(kbase);
        ExceptionWorkItemHandler handler = new ExceptionWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", handler);

        KogitoProcessRuntime kruntime = KogitoProcessRuntime.asKogitoProcessRuntime(ksession);
        KogitoProcessInstance processInstance = kruntime.startProcess("com.sample.bpmn.hello");

        assertNodeTriggered(processInstance.getStringId(), "Start", "User Task", "MyBoundaryErrorEvent");
    }

    @Test
    public void testBoundaryErrorEventDefaultHandlerWithoutErrorCodeWithoutStructureRef() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-BoundaryErrorEventDefaultHandlerWithoutErrorCodeWithoutStructureRef.bpmn2");
        ksession = createKnowledgeSession(kbase);
        ExceptionWorkItemHandler handler = new ExceptionWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", handler);

        KogitoProcessRuntime kruntime = KogitoProcessRuntime.asKogitoProcessRuntime(ksession);
        KogitoProcessInstance processInstance = kruntime.startProcess("com.sample.bpmn.hello");

        assertNodeTriggered(processInstance.getStringId(), "Start", "User Task", "MyBoundaryErrorEvent");
    }

    @Test
    public void testBoundaryErrorEventSubProcessExceptionMapping() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-BoundaryErrorEventSubProcessExceptionMapping.bpmn2");
        ksession = createKnowledgeSession(kbase);
        ExceptionWorkItemHandler handler = new ExceptionWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", handler);

        KogitoProcessRuntime kruntime = KogitoProcessRuntime.asKogitoProcessRuntime(ksession);
        KogitoProcessInstance processInstance = kruntime.startProcess("com.sample.bpmn.hello");

        assertEquals("java.lang.RuntimeException", getProcessVarValue(processInstance, "var1"));
    }

    @Test
    public void testBoundaryErrorEventStructureRef() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-BoundaryErrorEventStructureRef.bpmn2");
        ksession = createKnowledgeSession(kbase);
        ExceptionWorkItemHandler handler = new ExceptionWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", handler);

        KogitoProcessRuntime kruntime = KogitoProcessRuntime.asKogitoProcessRuntime(ksession);
        KogitoProcessInstance processInstance = kruntime.startProcess("com.sample.bpmn.hello");

        assertNodeTriggered(processInstance.getStringId(), "Start", "User Task", "MyBoundaryErrorEvent");
    }

    class ExceptionWorkItemHandler implements WorkItemHandler {

        @Override
        public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
            throw new RuntimeException();
        }

        @Override
        public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
        }

    }

    class WorkItemExecutionErrorWorkItemHandler implements WorkItemHandler {

        @Override
        public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
            throw new WorkItemExecutionError("500");
        }

        @Override
        public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
        }

    }
}
