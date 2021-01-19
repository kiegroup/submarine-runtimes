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
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;

import org.drools.core.common.ActivationsFilter;
import org.drools.core.common.BaseNode;
import org.drools.core.common.InternalFactHandle;
import org.drools.core.common.InternalWorkingMemory;
import org.drools.core.common.QueryElementFactHandle;
import org.drools.core.impl.InternalKnowledgeBase;
import org.drools.core.marshalling.impl.ClassObjectMarshallingStrategyAcceptor;
import org.drools.core.marshalling.impl.KogitoSerializablePlaceholderResolverStrategy;
import org.drools.core.marshalling.impl.MarshallerReaderContext;
import org.drools.core.marshalling.impl.ObjectMarshallingStrategyStoreImpl;
import org.drools.core.marshalling.impl.TupleKey;
import org.drools.core.phreak.PhreakTimerNode;
import org.drools.core.reteoo.LeftTuple;
import org.drools.core.reteoo.ObjectTypeConf;
import org.drools.core.rule.EntryPointId;
import org.drools.core.spi.PropagationContext;
import org.drools.core.spi.Tuple;
import org.kie.api.definition.process.Process;
import org.kie.api.marshalling.ObjectMarshallingStrategy;
import org.kie.api.marshalling.ObjectMarshallingStrategyStore;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.EnvironmentName;

public class KogitoMarshallerReaderContext extends DefaultedMarshallerReaderContext {

    public final boolean                                                           marshalProcessInstances;
    public final boolean                                                           marshalWorkItems;
    public final Environment                                                       env;
    private final ObjectMarshallingStrategyStore                                   resolverStrategyFactory;
    private final Map<Integer, ObjectMarshallingStrategy>                           usedStrategies;
    private final Map<ObjectMarshallingStrategy, ObjectMarshallingStrategy.Context> strategyContexts;
    public Map<String, Process> processes = new HashMap<>();
    private Object                                                                 parameterObject;

    public KogitoMarshallerReaderContext(InputStream stream,
                                         Map<String, Process> processes,
                                         Environment env) throws IOException {
        super(stream);
        ObjectMarshallingStrategy[] strats = (ObjectMarshallingStrategy[]) env.get(EnvironmentName.OBJECT_MARSHALLING_STRATEGIES );
        if ( strats == null ) {
            strats = getMarshallingStrategy();
        }
        this.resolverStrategyFactory = new ObjectMarshallingStrategyStoreImpl(strats );
        this.usedStrategies = new HashMap<>();
        this.strategyContexts = new HashMap<>();

        this.marshalProcessInstances = true;
        this.marshalWorkItems = true;
        this.env = env;

        this.parameterObject = null;

        this.processes = processes;
    }

    protected ObjectMarshallingStrategy[] getMarshallingStrategy() {
        return new ObjectMarshallingStrategy[]{new KogitoSerializablePlaceholderResolverStrategy(ClassObjectMarshallingStrategyAcceptor.DEFAULT)};
    }

    public Process getProcess(String processId) {
        return processes.get(processId);
    }

    public ObjectMarshallingStrategyStore getResolverStrategyFactory() {
        return resolverStrategyFactory;
    }

    public Map<Integer, ObjectMarshallingStrategy> getUsedStrategies() {
        return usedStrategies;
    }

    public Map<ObjectMarshallingStrategy, ObjectMarshallingStrategy.Context> getStrategyContexts() {
        return strategyContexts;
    }

    public Object getParameterObject() {
        return parameterObject;
    }

    public void setParameterObject(Object parameterObject) {
        this.parameterObject = parameterObject;
    }
}

/**
 * This class has the only purpose of highlighting useless methods that may be removed from the interface
 */
abstract class DefaultedMarshallerReaderContext extends ObjectInputStream implements MarshallerReaderContext {

    public Map<Integer, Map<TupleKey, PhreakTimerNode.Scheduler>> timerNodeSchedulers = new HashMap<>();

    public DefaultedMarshallerReaderContext(InputStream in) throws IOException {
        super(in);
    }

    public void setWorkingMemory(InternalWorkingMemory internalWorkingMemory) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addTimerNodeScheduler(int nodeId, TupleKey key, PhreakTimerNode.Scheduler scheduler) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public PhreakTimerNode.Scheduler removeTimerNodeScheduler(int nodeId, TupleKey key) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public InternalWorkingMemory getWorkingMemory() {
        return null;
    }

    @Override
    public InternalKnowledgeBase getKnowledgeBase() {
        return null;
    }

    @Override
    public Map<Long, InternalFactHandle> getHandles() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Map<Integer, LeftTuple> getTerminalTupleMap() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public ActivationsFilter getFilter() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Map<Integer, BaseNode> getSinks() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Map<Long, PropagationContext> getPropagationContexts() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Map<Integer, Object> getNodeMemories() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public ObjectMarshallingStrategyStore getResolverStrategyFactory() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public ClassLoader getClassLoader() {
        return null;
    }

    @Override
    public Map<Integer, ObjectMarshallingStrategy> getUsedStrategies() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Map<ObjectMarshallingStrategy, ObjectMarshallingStrategy.Context> getStrategyContexts() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Object getReaderForInt(int i) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setReaderForInt(int i, Object reader) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public InternalFactHandle createAccumulateHandle(EntryPointId entryPointId, InternalWorkingMemory workingMemory, LeftTuple leftTuple, Object result, int nodeId) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public InternalFactHandle createAsyncNodeHandle(Tuple leftTuple, InternalWorkingMemory workingMemory, Object object, int nodeId, ObjectTypeConf objectTypeConf) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public QueryElementFactHandle createQueryResultHandle(Tuple leftTuple, InternalWorkingMemory workingMemory, Object[] objects, int nodeId) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public InternalFactHandle createQueryHandle(Tuple leftTuple, InternalWorkingMemory workingMemory, int nodeId) {
        throw new UnsupportedOperationException("not implemented");
    }

    public void withSerializedNodeMemories() {
        throw new UnsupportedOperationException("not implemented");
    }
}
