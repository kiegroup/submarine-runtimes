/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

package org.kie.kogito.codegen.utils;

import org.kie.kogito.codegen.ApplicationGenerator;
import org.kie.kogito.codegen.context.KogitoBuildContext;
import org.kie.kogito.codegen.decision.DecisionCodegen;
import org.kie.kogito.codegen.prediction.PredictionCodegen;
import org.kie.kogito.codegen.process.ProcessCodegen;
import org.kie.kogito.codegen.rules.IncrementalRuleCodegen;

/**
 * Utility class that performs automatic ApplicationGenerator discovery
 */
public class ApplicationGeneratorDiscovery {

    private ApplicationGeneratorDiscovery() {
        // utility class
    }

    public static ApplicationGenerator discover(KogitoBuildContext context) {
        ApplicationGenerator appGen = new ApplicationGenerator(context);

        // configure each individual generator. Ordering is relevant.

        appGen.registerGeneratorIfEnabled(ProcessCodegen.fromContext(context));

        boolean useRestServices = context.hasClassAvailable("javax.ws.rs.Path")
                || context.hasClassAvailable("org.springframework.web.bind.annotation.RestController");
        appGen.registerGeneratorIfEnabled(IncrementalRuleCodegen.fromContext(context))
                .map(gen -> gen.withRestServices(useRestServices));

        appGen.registerGeneratorIfEnabled(PredictionCodegen.fromContext(context));

        appGen.registerGeneratorIfEnabled(DecisionCodegen.fromContext(context));

        return appGen;

    }
}