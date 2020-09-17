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

import io.quarkus.test.QuarkusUnitTest;
import io.restassured.http.ContentType;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.kie.kogito.integrationtests.HelloService;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.emptyOrNullString;

public class FlexibleProcessTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create( JavaArchive.class)
                    .addAsResource("AdHocProcess.bpmn.test", "src/main/resources/AdHocProcess.bpmn")
                    .addClasses( HelloService.class ));
    @Test
    void testInstantiateProcess() {
        Map<String, String> params = new HashMap<>();
        params.put("var1", "first");
        params.put("var2", "second");

        String pid = given()
                .contentType(ContentType.JSON)
            .when()
                .body(params)
                .post("/AdHocProcess")
            .then()
                .statusCode(201)
                .header("Location", not(emptyOrNullString()))
                .body("id", not(emptyOrNullString()))
                .body("var1", equalTo("Hello first! Script"))
                .body("var2", equalTo("second Script 2"))
            .extract()
                .path("id");

        given()
                .contentType(ContentType.JSON)
            .when()
                .get("/AdHocProcess/{pid}", pid)
            .then()
                .statusCode(200);
    }

    @Test
    void testProcessException() {
        Map<String, String> params = new HashMap<>();
        params.put("var1", "exception");
        params.put("var2", "second");

        given()
            .contentType(ContentType.JSON)
            .when()
            .body(params)
            .post("/AdHocProcess")
            .then()
            .statusCode(500);
    }
}
