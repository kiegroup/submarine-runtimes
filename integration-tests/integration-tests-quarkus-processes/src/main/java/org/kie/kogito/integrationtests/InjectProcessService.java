/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.kie.kogito.integrationtests;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.kie.kogito.process.Process;

@ApplicationScoped
public class InjectProcessService {

    @Inject
    @Named("injectProcess")
    Process<?> process;

    public void validate(String pid) {
        if (pid == null) {
            throw new IllegalArgumentException("pid is null");
        }
        if (process == null) {
            throw new IllegalArgumentException("process has not been injected");
        }
        if (!process.instances().findById(pid).isPresent()) {
            throw new IllegalStateException("Unable to find pid in the existing process instances: " + pid);
        }
    }
}
