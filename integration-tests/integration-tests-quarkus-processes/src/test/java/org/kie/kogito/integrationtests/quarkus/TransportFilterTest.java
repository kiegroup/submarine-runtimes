/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.kie.kogito.integrationtests.quarkus;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.jupiter.api.Test;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.testcontainers.quarkus.InfinispanQuarkusTestResource;
import org.kie.kogito.transport.TransportConfig;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.emptyOrNullString;

@QuarkusTest
@QuarkusTestResource(InfinispanQuarkusTestResource.Conditional.class)
class TransportFilterTest {

    static {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Inject
    @Named("AdHocFragments")
    Process<?> process;

    @Test
    void testTransportHeaders() {
        Map<String, String> params = new HashMap<>();
        params.put("var1", "Kermit");

        String id = given()
                .contentType(ContentType.JSON)
                .header("example1", "x")
                .header("Bar", "y")
                .header("OTHER", "z")
                .when()
                .body(params)
                .post("/AdHocFragments")
                .then()
                .statusCode(201)
                .body("id", not(emptyOrNullString()))
                .body("var1", equalTo("Kermit"))
                .header("Location", not(emptyOrNullString()))
                .extract()
                .path("id");

        Optional<? extends ProcessInstance<?>> processInstance = process.instances().findById(id);
        assertThat(processInstance).isPresent();
        Object context = processInstance.get().getContextAttr(TransportConfig.TRANSPORT_CONTEXT);
        assertThat(context).isInstanceOf(Map.class).isNotNull();
        Map<String, String> transportContext = (Map<String, String>) context;
        assertThat(transportContext).containsKeys("example1", "OTHER").doesNotContainKey("Bar");
    }
}
