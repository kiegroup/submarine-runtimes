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

package org.jbpm.integrationtests;

import org.drools.core.impl.InternalKnowledgeBase;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JbpmSerializationHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(JbpmSerializationHelper.class);


    public static KieSession getSerialisedStatefulKnowledgeSession(KieSession session, boolean ignored) {
        LOGGER.warn("This is mocked as a no-op");
        return session;
    }

    public static KieSession getSerialisedStatefulKnowledgeSession(KieSession session) {
        LOGGER.warn("This is mocked as a no-op");
        return session;
    }

    public static InternalKnowledgeBase serializeObject(InternalKnowledgeBase kbase) {
        LOGGER.warn("This is mocked as a no-op");
        return kbase;
    }
}
