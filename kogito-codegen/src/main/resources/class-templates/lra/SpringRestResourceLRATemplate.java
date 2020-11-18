package com.myspace.demo;

import java.util.List;
import java.util.Map;

import org.jbpm.util.JsonSchemaUtil;
import org.kie.api.runtime.process.WorkItemNotFoundException;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.WorkItem;
import org.kie.kogito.process.impl.Sig;
import org.kie.kogito.services.uow.UnitOfWorkExecutor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;

public class $Type$Resource {

    @PutMapping(value = "/{id}/lra/{action}")
    public ResponseEntity onLRA(@PathVariable("id") String id,
                          @PathVariable("action") String action,
                          @RequestHeader(org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_CONTEXT_HEADER) String lraUri) {
        return UnitOfWorkExecutor.executeInUnitOfWork(application.unitOfWorkManager(), () -> {
            java.util.Optional<ProcessInstance<$Type$>> processInstanceFound = process.instances().findById(id);
            if(processInstanceFound.isPresent()) {
                processInstanceFound.get().send(Sig.of("LRA-" + action, lraUri));
            } else if (io.narayana.lra.LRAConstants.COMPENSATE.equals(action)) {
                throw new NotFoundException();
            }
            return ResponseEntity.ok().build();
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
