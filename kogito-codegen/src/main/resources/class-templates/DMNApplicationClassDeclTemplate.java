
public class DecisionModels implements org.kie.kogito.decision.DecisionModels {

    private final static org.kie.pmml.evaluator.api.executor.PMMLRuntime pmmlRuntime = org.kie.kogito.app.PredictionModels.pmmlRuntime;
    private final static org.kie.dmn.api.core.DMNRuntime dmnRuntime = org.kie.kogito.dmn.DMNKogito.createGenericDMNRuntime(pmmlRuntime);
    private final static org.kie.kogito.ExecutionIdSupplier execIdSupplier = null;

    public void init(org.kie.kogito.Application app) {
        app.config().decision().decisionEventListeners().listeners().forEach(dmnRuntime::addListener);
    }

    public org.kie.kogito.decision.DecisionModel getDecisionModel(java.lang.String namespace, java.lang.String name) {
        return new org.kie.kogito.dmn.DmnDecisionModel(dmnRuntime, namespace, name, execIdSupplier);
    }

}
