/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.process;

import java.util.Collection;
import java.util.Optional;

public interface ProcessInstances<T> {

    default Optional<ProcessInstance<T>> findById(String id) {
        return findById(id, ProcessInstanceReadMode.MUTABLE);
    }

    Optional<ProcessInstance<T>> findById(String id, ProcessInstanceReadMode mode);

    default Collection<ProcessInstance<T>> values() {
        return values(ProcessInstanceReadMode.READ_ONLY);
    }

    default Optional<ProcessInstance<T>> findByBusinessKey(String businessKey) {
        return findByBusinessKey(businessKey, ProcessInstanceReadMode.READ_ONLY);
    }

    default Optional<ProcessInstance<T>> findByBusinessKey(String businessKey, ProcessInstanceReadMode mode) {
        for (ProcessInstance<T> instance : values(mode)) {
            if (businessKey.equals(instance.businessKey())) {
                return Optional.of(instance);
            }
        }
        return Optional.empty();
    }

    Collection<ProcessInstance<T>> values(ProcessInstanceReadMode mode);

    Integer size();
}
