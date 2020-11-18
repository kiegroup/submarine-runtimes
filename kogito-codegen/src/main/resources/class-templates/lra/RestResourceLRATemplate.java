package com.myspace.demo;

import java.util.Optional;

import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.impl.Sig;
import org.kie.kogito.services.uow.UnitOfWorkExecutor;

public class $Type$Resource {

    @PUT
    @Path("/{id}/lra/{action}")
    public Response onLRA(@PathParam("id") String id,
                          @PathParam("action") String action,
                          @javax.ws.rs.HeaderParam(org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_CONTEXT_HEADER) String lraUri) {
        return UnitOfWorkExecutor.executeInUnitOfWork(application.unitOfWorkManager(), () -> {
            java.util.Optional<ProcessInstance<$Type$>> processInstanceFound = process.instances().findById(id);
            if(processInstanceFound.isPresent()) {
                processInstanceFound.get().send(Sig.of("LRA-" + action, lraUri));
            } else if (io.narayana.lra.LRAConstants.COMPENSATE.equals(action)) {
                throw new NotFoundException();
            }
            return Response.ok().build();
        });
    }

    private void addLRAMetadata(ProcessInstance<$Type$> processInstance, HttpHeaders httpHeaders, URI resourceUri) {
        String lraUri = httpHeaders.getHeaderString(org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_CONTEXT_HEADER);
        if(lraUri != null) {
            ((AbstractProcessInstance<$Type$>)processInstance)
                    .internalGetProcessInstance()
                    .setMetaData(
                            org.kie.kogito.lra.KogitoLRA.LRA_CONTEXT,
                            new org.kie.kogito.lra.model.LRAContext().setUri(URI.create(lraUri)).setBasePath(resourceUri)
                    );
        }
    }

}
