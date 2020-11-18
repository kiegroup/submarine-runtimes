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

package org.kie.kogito.lra.client;

import java.net.URISyntaxException;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.inject.Named;

import io.narayana.lra.client.NarayanaLRAClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.kie.kogito.lra.KogitoLRA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Dependent
public class LRAClientProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(LRAClientProducer.class);

    @ConfigProperty(name = NarayanaLRAClient.LRA_COORDINATOR_HOST_KEY, defaultValue = "localhost")
    String host;

    @ConfigProperty(name = NarayanaLRAClient.LRA_COORDINATOR_PORT_KEY, defaultValue = "8080")
    Integer port;

    @Produces
    @Named(KogitoLRA.BEAN_NAME)
    @ApplicationScoped
    NarayanaLRAClient getClient() throws URISyntaxException {
        LOGGER.info("Configuring Narayana LRA Client to {}:{}", host, port);
        return new NarayanaLRAClient(host, port);
    }
}
