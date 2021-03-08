/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.tck.junit.asserts;

import org.kie.kogito.tck.junit.listeners.TrackingProcessEventListener;

public class TrackProcessPredicateAssert {

    private TrackingProcessEventListener tracker;

    public TrackProcessPredicateAssert(TrackingProcessEventListener tracker) {
        this.tracker = tracker;
    }

    public IterableTrackProcessPredicateAssert checkStepsForProcessInstance(String id) {
        return new IterableTrackProcessPredicateAssert(tracker.eventsForProcess(id));
    }

    public ListTrackProcessPredicateAssert checkEventsProcessInstanceThat(String id) {
        return new ListTrackProcessPredicateAssert(tracker.eventsForProcess(id));
    }
}
