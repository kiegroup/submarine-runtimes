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

package org.jbpm.process;

import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.test.util.AbstractBaseTest;
import org.jbpm.workflow.core.JbpmNode;
import org.jbpm.workflow.core.impl.ConnectionImpl;
import org.jbpm.workflow.core.node.EndNode;
import org.jbpm.workflow.core.node.StartNode;
import org.jbpm.workflow.core.node.SubProcessNode;
import org.junit.jupiter.api.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.kogito.process.runtime.KogitoProcessInstance;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SubProcessTest extends AbstractBaseTest  {
    
    public void addLogger() { 
        logger = LoggerFactory.getLogger(this.getClass());
    }
  
	@Test
    public void testNonExistentSubProcess() {
	    String nonExistentSubProcessName = "nonexistent.process";
        RuleFlowProcess process = new RuleFlowProcess();
        process.setId("org.drools.core.process.process");
        process.setName("Process");
        StartNode startNode = new StartNode();
        startNode.setName("Start");
        startNode.setId(1);
        SubProcessNode subProcessNode = new SubProcessNode();
        subProcessNode.setName("SubProcessNode");
        subProcessNode.setId(2);
        subProcessNode.setProcessId(nonExistentSubProcessName);
        EndNode endNode = new EndNode();
        endNode.setName("End");
        endNode.setId(3);
        
        connect(startNode, subProcessNode);
        connect(subProcessNode, endNode);
        
        process.addNode( startNode );
        process.addNode( subProcessNode );
        process.addNode( endNode );

        KieSession ksession = createKieSession(process);
        
        ProcessInstance pi = ksession.startProcess("org.drools.core.process.process");
        assertEquals( KogitoProcessInstance.STATE_ERROR, pi.getState());
    }
    
	private void connect( JbpmNode sourceNode, JbpmNode targetNode) {
		new ConnectionImpl(sourceNode, JbpmNode.CONNECTION_DEFAULT_TYPE,
				           targetNode, JbpmNode.CONNECTION_DEFAULT_TYPE);
	}

}
