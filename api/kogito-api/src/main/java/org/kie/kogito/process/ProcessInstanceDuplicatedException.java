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

package org.kie.kogito.process;

public class ProcessInstanceDuplicatedException extends RuntimeException {

    private static final long serialVersionUID = 8031225233775014572L;

    private final String processInstanceId;
    
    public ProcessInstanceDuplicatedException(String processInstanceId) {
        super("Process instance with id '" + processInstanceId + "' already exists, usually this means business key has been already used");
        this.processInstanceId = processInstanceId;
    }

    public ProcessInstanceDuplicatedException(String processInstanceId, Throwable cause) {
        super("Process instance with '" + processInstanceId + "' already exists, usually this means business key has been already used");
        this.processInstanceId = processInstanceId;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

}
