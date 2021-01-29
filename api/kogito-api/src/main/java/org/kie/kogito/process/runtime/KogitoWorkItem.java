/*
 * Copyright 2005 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.process.runtime;

import java.util.Date;
import java.util.Map;

import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.WorkItem;
import org.kie.kogito.process.workitem.Policy;

public interface KogitoWorkItem extends WorkItem {

    String getStringId();

    /**
     * The id of the process instance that requested the execution of this
     * work item
     *
     * @return the id of the related process instance
     */
    String getProcessInstanceStringId();

    /**
     * Returns optional life cycle phase id associated with this work item
     * @return optional life cycle phase id
     */
    String getPhaseId();

    /**
     * Returns optional life cycle phase status associated with this work item
     * @return optional life cycle phase status
     */
    String getPhaseStatus();

    /**
     * Returns timestamp indicating the start date of this work item
     * @return start date
     */
    Date getStartDate();

    /**
     * Returns timestamp indicating the completion date of this work item
     * @return completion date
     */
    Date getCompleteDate();

    /**
     * The node instance that is associated with this
     * work item
     *
     * @return the related node instance
     */
    NodeInstance getNodeInstance();

    /**
     * The process instance that requested the execution of this
     * work item
     *
     * @return the related process instance
     */
    KogitoProcessInstance getProcessInstance();

    /**
     * Enforces given policies on this work item. It must false in case of any policy
     * violations.
     * @param policies optional policies to be enforced
     * @return return true if this work item can enforce all policies otherwise false
     */
    default boolean enforce( Policy<?>...policies) {
        return true;
    }

    static KogitoWorkItem adapt( WorkItem workItem ) {
        return workItem instanceof KogitoProcessInstance ?
                ( KogitoWorkItem ) workItem :
                new KogitoWorkItemAdapter( workItem );
    }

    class KogitoWorkItemAdapter implements KogitoWorkItem {

        private final WorkItem delegate;

        public KogitoWorkItemAdapter( WorkItem delegate ) {
            this.delegate = delegate;
        }

        @Override
        public long getId() {
            return delegate.getId();
        }

        @Override
        public String getName() {
            return delegate.getName();
        }

        @Override
        public int getState() {
            return delegate.getState();
        }

        @Override
        public Object getParameter( String s ) {
            return delegate.getParameter( s );
        }

        @Override
        public Map<String, Object> getParameters() {
            return delegate.getParameters();
        }

        @Override
        public Object getResult( String s ) {
            return delegate.getResult( s );
        }

        @Override
        public Map<String, Object> getResults() {
            return delegate.getResults();
        }

        @Override
        public long getProcessInstanceId() {
            return delegate.getProcessInstanceId();
        }

        @Override
        public String getStringId() {
            return "" + getId();
        }

        @Override
        public String getProcessInstanceStringId() {
            return "" + getProcessInstanceId();
        }

        @Override
        public String getPhaseId() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getPhaseStatus() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Date getStartDate() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Date getCompleteDate() {
            throw new UnsupportedOperationException();
        }

        @Override
        public NodeInstance getNodeInstance() {
            throw new UnsupportedOperationException();
        }

        @Override
        public KogitoProcessInstance getProcessInstance() {
            throw new UnsupportedOperationException();
        }
    }
}