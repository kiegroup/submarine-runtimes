/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.kie.kogito.rules.units;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.kie.internal.ruleunit.RuleUnitVariable;
import org.kie.kogito.rules.RuleUnitConfig;

public abstract class AbstractRuleUnitDescription implements KogitoRuleUnitDescription {

    private final Map<String, KogitoRuleUnitVariable> varDeclarations = new HashMap<>();
    private RuleUnitConfig config;

    @Override
    public Optional<Class<?>> getDatasourceType(String name) {
        return Optional.ofNullable(varDeclarations.get(name))
                .filter(RuleUnitVariable::isDataSource)
                .map(RuleUnitVariable::getDataSourceParameterType);
    }

    @Override
    public Optional<Class<?>> getVarType(String name) {
        return Optional.ofNullable(varDeclarations.get(name)).map(RuleUnitVariable::getType);
    }

    @Override
    public boolean hasVar(String name) {
        return varDeclarations.containsKey(name);
    }

    @Override
    public KogitoRuleUnitVariable getVar(String name) {
        KogitoRuleUnitVariable ruleUnitVariable = (KogitoRuleUnitVariable) varDeclarations.get(name);
        if (ruleUnitVariable == null) {
            throw new UndefinedRuleUnitVariable(name, this.getCanonicalName());
        }
        return ruleUnitVariable;
    }

    @Override
    public Collection<String> getUnitVars() {
        return varDeclarations.keySet();
    }

    @Override
    public Collection<KogitoRuleUnitVariable> getUnitVarDeclarations() {
        return varDeclarations.values();
    }

    @Override
    public boolean hasDataSource(String name) {
        RuleUnitVariable ruleUnitVariable = varDeclarations.get(name);
        return ruleUnitVariable != null && ruleUnitVariable.isDataSource();
    }

    protected void putRuleUnitVariable(KogitoRuleUnitVariable varDeclaration) {
        varDeclarations.put(varDeclaration.getName(), varDeclaration);
    }

    protected void setConfig(RuleUnitConfig config) {
        this.config = config;
    }

    @Override
    public RuleUnitConfig getConfig() {
        return config;
    }
}
