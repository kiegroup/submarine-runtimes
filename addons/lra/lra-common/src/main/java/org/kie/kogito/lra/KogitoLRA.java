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

package org.kie.kogito.lra;

import org.eclipse.microprofile.lra.annotation.ws.rs.LRA.Type;

public class KogitoLRA {

    public static final String BEAN_NAME = "kogito-lra";
    public static final String METADATA_TIMEOUT = "LRA_timeout";
    public static final String METADATA_TYPE = "LRA_type";
    public static final Type DEFAULT_LRA_TYPE = Type.REQUIRED;
    public static final String LRA_CONTEXT = "lra_context";
    public static final String LRA_RESOURCE = "lra";

    private KogitoLRA() {
    }
}
