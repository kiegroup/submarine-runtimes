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

package org.kie.kogito.lra.model;

import java.io.Serializable;
import java.net.URI;

public class LRAContext implements Serializable {

    private URI basePath;
    private URI uri;
    private URI parentUri;
    private URI recoverUri;

    public URI getBasePath() {
        return basePath;
    }

    public LRAContext setBasePath(URI basePath) {
        this.basePath = basePath;
        return this;
    }

    public URI getUri() {
        return uri;
    }

    public LRAContext setUri(URI uri) {
        this.uri = uri;
        return this;
    }

    public URI getParentUri() {
        return parentUri;
    }

    public LRAContext setParentUri(URI parentUri) {
        this.parentUri = parentUri;
        return this;
    }

    public URI getRecoverUri() {
        return recoverUri;
    }

    public LRAContext setRecoverUri(URI recoverUri) {
        this.recoverUri = recoverUri;
        return this;
    }
}
