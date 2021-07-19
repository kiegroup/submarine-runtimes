/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.services.event.impl;

import org.kie.kogito.event.EventConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonStringToObject<T> implements EventConverter<String, T> {

    private static final Logger logger = LoggerFactory.getLogger(JsonStringToObject.class);
    private final Class<T> clazz;
    private final ObjectMapper objectMapper;

    public JsonStringToObject(ObjectMapper objectMapper, Class<T> clazz) {
        this.objectMapper = objectMapper;
        this.clazz = clazz;
    }

    @Override
    public T apply(String value) throws JsonProcessingException {
        logger.debug("Converting event with payload {} to class {} ", value, clazz);
        return objectMapper.readValue(value, clazz);
    }

    @Override
    public String toString() {
        return "JsonStringToObject [clazz=" + clazz + "]";
    }

    @Override
    public Class<T> getOutputClass() {
        return clazz;
    }
}
