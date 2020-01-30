/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.codegen.rules.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.kie.kogito.codegen.GeneratorContext;
import org.kie.kogito.conf.ClockType;
import org.kie.kogito.conf.EventProcessingType;
import org.kie.kogito.rules.RuleUnitConfig;

public final class NamedRuleUnitConfig {

    private static String CONFIG_PREFIX = "kogito.rules.";
    private static String CONFIG_EVENT_PROCESSING_TYPE = CONFIG_PREFIX + "\"%s\".event-processing-type";
    private static String CONFIG_CLOCK_TYPE = CONFIG_PREFIX + "\"%s\".clock-type";
    private static String CONFIG_SESSIONS_POOL = CONFIG_PREFIX + "\"%s\".sessions-pool";

    public static List<NamedRuleUnitConfig> fromContext(GeneratorContext context) {
        HashSet<String> canonicalNames = new HashSet<>();
        for (String k : context.getApplicationProperties()) {
            if (k.startsWith(CONFIG_PREFIX)) {
                String rest = k.substring(CONFIG_PREFIX.length());
                Optional<String> unitCanonicalName = parseQuotedIdentifier(rest);
                unitCanonicalName.ifPresent(canonicalNames::add);
            }
        }

        ArrayList<NamedRuleUnitConfig> configs = new ArrayList<>();
        for (String canonicalName : canonicalNames) {
            String ept = context.getApplicationProperty(
                    String.format(CONFIG_EVENT_PROCESSING_TYPE, canonicalName))
                    .orElse(EventProcessingType.CLOUD.name())
                    .toUpperCase();
            EventProcessingType eventProcessingType = EventProcessingType.valueOf(ept);

            String ct = context.getApplicationProperty(
                    String.format(CONFIG_CLOCK_TYPE, canonicalName))
                    .orElse(ClockType.REALTIME.name())
                    .toUpperCase();
            ClockType clockType = ClockType.valueOf(ct);

            Optional<String> sp = context.getApplicationProperty(
                    String.format(CONFIG_SESSIONS_POOL, canonicalName));
            Integer sessionPool = sp.map(Integer::parseInt).orElse(null);

            configs.add(new NamedRuleUnitConfig(
                    canonicalName,
                    new RuleUnitConfig(
                            eventProcessingType,
                            clockType,
                            sessionPool)));
        }

        return configs;
    }

    private static Optional<String> parseQuotedIdentifier(String key) {
        if (key.startsWith("\"")) {
            int endIndex = key.substring(1).indexOf('"');
            if (endIndex == -1) {
                return Optional.empty();
            } else {
                return Optional.of(key.substring(1, endIndex + 1));
            }
        } else {
            return Optional.empty();
        }
    }

    private final String canonicalName;
    private final RuleUnitConfig config;

    public NamedRuleUnitConfig(String canonicalName, RuleUnitConfig config) {
        this.canonicalName = canonicalName;
        this.config = config;
    }

    public String getCanonicalName() {
        return canonicalName;
    }

    public RuleUnitConfig getConfig() {
        return config;
    }
}
