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

import io.quarkus.test.QuarkusUnitTest;
import io.restassured.http.ContentType;
import org.acme.travels.Address;
import org.acme.travels.Traveller;
import org.acme.travels.TravellerValidationService;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

public class JsonSchemaTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create( JavaArchive.class)
                    .addAsResource("approval.bpmn.test", "src/main/resources/approval.bpmn")
                    .addAsResource("approvals_firstLineApproval.json.test", "META-INF/jsonSchema/approvals_firstLineApproval.json")
                    .addClasses( Address.class, Traveller.class, TravellerValidationService.class ));
    @Test
    void testJsonSchema() {
        given().contentType(ContentType.JSON).when().get("/approvals/firstLineApproval/schema").then().statusCode(200).body(matchesJsonSchemaInClasspath("META-INF/jsonSchema/approvals_firstLineApproval.json"));
    }
}
