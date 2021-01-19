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

package org.jbpm.marshalling.impl;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import org.drools.core.common.BaseNode;
import org.drools.core.common.InternalWorkingMemory;
import org.drools.core.impl.InternalKnowledgeBase;
import org.drools.core.marshalling.impl.MarshallerWriteContext;
import org.drools.core.marshalling.impl.ObjectMarshallingStrategyStoreImpl;
import org.kie.api.marshalling.ObjectMarshallingStrategy;
import org.kie.api.marshalling.ObjectMarshallingStrategyStore;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.EnvironmentName;
import org.kie.internal.marshalling.MarshallerFactory;

/**
 * Extension to default <code>MarshallerWriteContext</code> that allows to pass additional
 * information to marshaller strategies, such as process instance id, task it, state
 */
public class KogitoProcessMarshallerWriteContext extends
                                                 ObjectOutputStream implements MarshallerWriteContext {
    
    public static final int STATE_ACTIVE = 1;
    public static final int STATE_COMPLETED = 2;
    public final PrintStream out = System.out;
    private final ObjectMarshallingStrategyStore                                   objectMarshallingStrategyStore;
    private final Map<ObjectMarshallingStrategy, Integer>                           usedStrategies;
    private final Map<ObjectMarshallingStrategy, ObjectMarshallingStrategy.Context> strategyContext;
    private final boolean                                                           marshalProcessInstances = true;
    private final boolean                                                           marshalWorkItems = true;
    private final Environment                                                       env;

    private String processInstanceId;
    private String taskId;
    private String workItemId;
    private int state;
    private long                                                                   clockTime;
    private Object                                                                 parameterObject;

    public KogitoProcessMarshallerWriteContext(OutputStream stream,
                                         Environment env) throws IOException {
        super(stream);

        ObjectMarshallingStrategy[] strats = (ObjectMarshallingStrategy[]) env.get(EnvironmentName.OBJECT_MARSHALLING_STRATEGIES );
        if ( strats == null ) {
            strats = getMarshallingStrategy();
        }
        this.objectMarshallingStrategyStore = new ObjectMarshallingStrategyStoreImpl(strats );
        this.usedStrategies = new HashMap<ObjectMarshallingStrategy, Integer>();
        this.strategyContext = new HashMap<ObjectMarshallingStrategy, ObjectMarshallingStrategy.Context>();
        this.env = env;
    }
    
    public String getProcessInstanceId() {
        return processInstanceId;
    }
    
    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }
    
    public String getTaskId() {
        return taskId;
    }
    
    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
    
    public String getWorkItemId() {
        return workItemId;
    }
    
    public void setWorkItemId(String workItemId) {
        this.workItemId = workItemId;
    }
    
    public int getState() {
        return state;
    }
    
    public void setState(int state) {
        this.state = state;
    }

    protected ObjectMarshallingStrategy[] getMarshallingStrategy() {
        return new ObjectMarshallingStrategy[]{MarshallerFactory.newSerializeMarshallingStrategy()};
    }

    @Override
    public Integer getStrategyIndex(ObjectMarshallingStrategy strategy) {
        Integer index = usedStrategies.get( strategy );
        if ( index == null ) {
            index = Integer.valueOf( usedStrategies.size() );
            usedStrategies.put( strategy, index );
            strategyContext.put( strategy, strategy.createContext() );
        }
        return index;
    }

    @Override
    public ObjectMarshallingStrategyStore getObjectMarshallingStrategyStore() {
        return objectMarshallingStrategyStore;
    }

    @Override
    public Object getParameterObject() {
        return parameterObject;
    }

    @Override
    public void setParameterObject( Object parameterObject ) {
        this.parameterObject = parameterObject;
    }

    @Override
    public Map<ObjectMarshallingStrategy, ObjectMarshallingStrategy.Context> getStrategyContext() {
        return strategyContext;
    }

    @Override
    public Map<ObjectMarshallingStrategy, Integer> getUsedStrategies() {
        return usedStrategies;
    }

    @Override
    public boolean isMarshalProcessInstances() {
        return marshalProcessInstances;
    }

    @Override
    public boolean isMarshalWorkItems() {
        return marshalWorkItems;
    }

    @Override
    public Environment getEnvironment() {
        return env;
    }

    @Override
    public InternalKnowledgeBase getKnowledgeBase() {
        return null;
    }

    @Override
    public InternalWorkingMemory getWorkingMemory() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<Integer, BaseNode> getSinks() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getClockTime() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setClockTime( long clockTime ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getWriterForClass(Class<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setWriterForClass(Class<?> c, Object writer) {
        throw new UnsupportedOperationException();
    }
}
