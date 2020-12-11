@javax.enterprise.context.ApplicationScoped()
public class DecisionModels extends org.kie.kogito.dmn.AbstractDecisionModels {

    static {
        init(
                PredictionModels.kieRuntimeFactoryFunction
                /* arguments provided during codegen */);
    }

    @javax.inject.Inject
    protected org.kie.kogito.Application application;

    public DecisionModels() {
        super();
    }

    @javax.annotation.PostConstruct
    protected void init() {
        initApplication(application);
    }
}