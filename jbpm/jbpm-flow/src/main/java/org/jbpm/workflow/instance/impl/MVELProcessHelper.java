/*
 *  Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.jbpm.workflow.instance.impl;

import java.util.function.Supplier;

import org.drools.core.util.MVELSafeHelper;
import org.kie.soup.project.datamodel.commons.util.MVELEvaluator;

public class MVELProcessHelper {
    public static final Supplier<MVELEvaluator> MVEL_SUPPLIER =
            System.getProperty("org.graalvm.nativeimage.imagecode") == null ?
                    MVELSafeHelper::getEvaluator
                    : () -> { throw new UnsupportedOperationException("MVEL evaluation is not supported in native image"); } ;


    public MVELEvaluator get() {
        return MVEL_SUPPLIER.get();
    }

}
