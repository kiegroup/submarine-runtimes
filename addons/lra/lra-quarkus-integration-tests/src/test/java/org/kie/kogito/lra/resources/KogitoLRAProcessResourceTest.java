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

package org.kie.kogito.lra.resources;

import java.net.URI;
import java.time.temporal.ChronoUnit;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import javax.json.Json;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import io.narayana.lra.LRAConstants;
import io.narayana.lra.client.NarayanaLRAClient;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.kogito.Model;
import org.kie.kogito.lra.KogitoLRA;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.testcontainers.quarkus.InfinispanQuarkusTestResource;
import org.mockito.Mockito;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_CONTEXT_HEADER;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@QuarkusTest
@QuarkusTestResource(InfinispanQuarkusTestResource.Conditional.class)
class KogitoLRAProcessResourceTest {

    static {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    private static final String MESSAGE = "hey there";

    UriBuilder uriTemplate;

    @Inject
    @Named("lraProcess")
    Process<? extends Model> process;

    NarayanaLRAClient lraClient;

    @BeforeEach
    void init() {
        lraClient = Mockito.mock(NarayanaLRAClient.class, KogitoLRA.BEAN_NAME);
        QuarkusMock.installMockForType(lraClient, NarayanaLRAClient.class);
        uriTemplate = UriBuilder.fromPath(RestAssured.baseURI + ":" + RestAssured.port)
                .path("lraProcess")
                .path("{pid}")
                .path(KogitoLRA.LRA_RESOURCE)
                .path("{action}");
    }

    @Test
    void testJoinAndCompensateLRA() {
        final URI lraUri = URI.create("org.example.com/lra-process/0001");
        final URI recoveryUri = URI.create("recovery/org.example.com/lra-rocess/0001");
        when(lraClient.startLRA(null, "lraProcess", 0L, ChronoUnit.SECONDS)).thenReturn(lraUri);
        when(lraClient.joinLRA(eq(lraUri), anyLong(), any(URI.class), any(URI.class), any(URI.class), any(URI.class), any(URI.class), nullable(URI.class), nullable(String.class)))
                .thenReturn(recoveryUri);

        String id = given()
                .header(LRA_HTTP_CONTEXT_HEADER, lraUri.toString())
                .body(Json.createObjectBuilder().add("message", MESSAGE).build().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .post("/lraProcess")
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .header("Location", not(emptyOrNullString()))
                .extract()
                .path("id");

        assertThat(process.instances().findById(id)).isPresent();
        ProcessInstance<? extends Model> instance = process.instances().findById(id).get();
        verifyJoinLRA(lraUri, instance);

        given()
                .header(LRA_HTTP_CONTEXT_HEADER, lraUri.toString())
                .put(uriTemplate.build(instance.id(), LRAConstants.COMPENSATE))
                .then()
                .statusCode(Response.Status.OK.getStatusCode());

        verifyProcessCompletedWith(LRAConstants.COMPENSATE, instance);
    }

    @Test
    void testJoinAndCompleteLRA() {
        final URI lraUri = URI.create("org.example.com/lra-process/0001");
        final URI recoveryUri = URI.create("recovery/org.example.com/lra-rocess/0001");
        when(lraClient.startLRA(null, "lraProcess", 0L, ChronoUnit.SECONDS)).thenReturn(lraUri);
        when(lraClient.joinLRA(eq(lraUri), anyLong(), any(URI.class), any(URI.class), any(URI.class), any(URI.class), any(URI.class), nullable(URI.class), nullable(String.class)))
                .thenReturn(recoveryUri);

        String id = given()
                .header(LRA_HTTP_CONTEXT_HEADER, lraUri.toString())
                .body(Json.createObjectBuilder().add("message", MESSAGE).build().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .post("/lraProcess")
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .header("Location", not(emptyOrNullString()))
                .extract()
                .path("id");

        assertThat(process.instances().findById(id)).isPresent();
        ProcessInstance<? extends Model> instance = process.instances().findById(id).get();
        verifyJoinLRA(lraUri, instance);

        given()
                .header(LRA_HTTP_CONTEXT_HEADER, lraUri.toString())
                .put(uriTemplate.build(instance.id(), LRAConstants.COMPLETE))
                .then()
                .statusCode(Response.Status.OK.getStatusCode());


        Mockito.verify(lraClient, times(0)).closeLRA(lraUri);

        verifyProcessCompletedWith(LRAConstants.COMPLETE, instance);
    }

    private void verifyJoinLRA(URI lraUri, ProcessInstance<? extends Model> instance) {
        Mockito.verify(lraClient, times(0)).startLRA(anyString());
        Mockito.verify(lraClient, times(1)).joinLRA(lraUri, 0L,
                uriTemplate.build(instance.id(), LRAConstants.COMPENSATE),
                uriTemplate.build(instance.id(), LRAConstants.COMPLETE),
                uriTemplate.build(instance.id(), LRAConstants.FORGET),
                uriTemplate.build(instance.id(), LRAConstants.LEAVE),
                uriTemplate.build(instance.id(), LRAConstants.AFTER),
                null,
                null);
    }

    private void verifyProcessCompletedWith(String action, ProcessInstance<? extends Model> instance) {
        assertThat(instance.status()).isEqualTo(ProcessInstance.STATE_COMPLETED);
        List<String> history = (List<String>) instance.variables().toMap().get("history");
        assertThat(history).containsExactly("hey there", "main task", action);
    }
}
