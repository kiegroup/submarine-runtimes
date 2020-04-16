package org.kie.kogito.tracing.decision;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.vertx.core.eventbus.EventBus;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.event.AfterEvaluateDecisionEvent;
import org.kie.dmn.api.core.event.BeforeEvaluateDecisionEvent;
import org.kie.dmn.api.core.event.DMNRuntimeEventListener;
import org.kie.kogito.decision.DecisionExecutionIdUtils;
import org.kie.kogito.tracing.decision.event.AfterEvaluateAllEvent;
import org.kie.kogito.tracing.decision.event.BeforeEvaluateAllEvent;
import org.kie.kogito.tracing.decision.event.EvaluateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class DecisionTracingListener implements DMNRuntimeEventListener {

    private static final Logger LOG = LoggerFactory.getLogger(DecisionTracingListener.class);

    @Inject
    EventBus bus;

    @Override
    public void beforeEvaluateAll(org.kie.dmn.api.core.event.BeforeEvaluateAllEvent event) {
        LOG.trace("beforeEvaluateAll");
        send(new BeforeEvaluateAllEvent(
                extractExecutionId(event.getResult().getContext()),
                event.getModelName(),
                event.getModelNamespace(),
                event.getResult().getContext()
        ));
    }

    @Override
    public void afterEvaluateAll(org.kie.dmn.api.core.event.AfterEvaluateAllEvent event) {
        LOG.trace("afterEvaluateAll");
        send(new AfterEvaluateAllEvent(
                extractExecutionId(event.getResult().getContext()),
                event.getModelName(),
                event.getModelNamespace(),
                event.getResult()
        ));
    }

    @Override
    public void beforeEvaluateDecision(BeforeEvaluateDecisionEvent event) {
        LOG.trace("beforeEvaluateDecision");
        send(new org.kie.kogito.tracing.decision.event.BeforeEvaluateDecisionEvent(
                extractExecutionId(event.getResult().getContext()),
                event.getDecision().getModelName(),
                event.getDecision().getModelNamespace(),
                event.getDecision().getId(),
                event.getResult()
        ));
    }

    @Override
    public void afterEvaluateDecision(AfterEvaluateDecisionEvent event) {
        LOG.trace("afterEvaluateDecision");
        send(new org.kie.kogito.tracing.decision.event.AfterEvaluateDecisionEvent(
                extractExecutionId(event.getResult().getContext()),
                event.getDecision().getModelName(),
                event.getDecision().getModelNamespace(),
                event.getDecision().getId(),
                event.getResult()
        ));
    }

    private String extractExecutionId(DMNContext context) {
        return DecisionExecutionIdUtils.get(context);
    }

    private void send(EvaluateEvent event) {
        bus.send(String.format("kogito-tracing-decision_%s", event.getClass().getSimpleName()), event);
    }

}
