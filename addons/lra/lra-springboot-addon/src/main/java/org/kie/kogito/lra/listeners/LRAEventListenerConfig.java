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

package org.kie.kogito.lra.listeners;

import io.narayana.lra.client.NarayanaLRAClient;
import org.kie.kogito.lra.KogitoLRA;
import org.kie.kogito.process.impl.DefaultProcessEventListenerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class LRAEventListenerConfig extends DefaultProcessEventListenerConfig {

    public LRAEventListenerConfig() {
    }

    @Autowired
    public LRAEventListenerConfig(@Qualifier(KogitoLRA.BEAN_NAME) NarayanaLRAClient lraClient) {
        super(new LRAEventListener(lraClient));
    }
}
