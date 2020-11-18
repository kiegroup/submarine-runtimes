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

import io.narayana.lra.client.NarayanaLRAClient;
import org.kie.kogito.lra.KogitoLRA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class LRAClientProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(LRAClientProducer.class);

    @Value("${" + NarayanaLRAClient.LRA_COORDINATOR_HOST_KEY + ":localhost}")
    String host;

    @Value("${" + NarayanaLRAClient.LRA_COORDINATOR_PORT_KEY + ":8080}")
    Integer port;

    @Bean
    @Qualifier(KogitoLRA.BEAN_NAME)
    NarayanaLRAClient getClient() throws URISyntaxException {
        LOGGER.info("Configuring Narayana LRA Client to {}:{}", host, port);
        return new NarayanaLRAClient(host, port);
    }
}
