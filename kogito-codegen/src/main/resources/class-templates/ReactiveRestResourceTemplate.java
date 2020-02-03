package com.myspace.demo;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;


import org.kie.api.runtime.process.WorkItemNotFoundException;
import org.kie.kogito.Application;
import org.kie.kogito.auth.SecurityPolicy;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.ProcessInstanceExecutionException;
import org.kie.kogito.process.WorkItem;
import org.kie.kogito.process.workitem.Policy;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/$name$")
public class $Type$ReactiveResource {

    Process<$Type$> process;
    
    Application application;

    @POST()
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)    
    public CompletionStage<$Type$> createResource_$name$($Type$ resource) {
        if (resource == null) {
            resource = new $Type$();
        }
        final $Type$ value = resource;
        return CompletableFuture.supplyAsync(() -> {
            return org.kie.kogito.services.uow.UnitOfWorkExecutor.executeInUnitOfWork(application.unitOfWorkManager(), () -> {
                ProcessInstance<$Type$> pi = process.createInstance(value);
                pi.start();
                return getModel(pi);
            });
        });
    }

    @GET()
    @Produces(MediaType.APPLICATION_JSON)
    public CompletionStage<List<$Type$>> getResources_$name$() {
        return CompletableFuture.supplyAsync(() -> {
            return process.instances().values().stream()
                    .map(ProcessInstance::variables)
                 .collect(Collectors.toList());
        });   
    }

    @GET()
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public CompletionStage<$Type$> getResource_$name$(@PathParam("id") String id) {
        return CompletableFuture.supplyAsync(() -> {
            return process.instances()
                    .findById(id)
                    .map(ProcessInstance::variables)
                    .orElse(null);
        });
    }
    
    @DELETE()
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public CompletionStage<$Type$> deleteResource_$name$(@PathParam("id") final String id) {
        return CompletableFuture.supplyAsync(() -> {
            return org.kie.kogito.services.uow.UnitOfWorkExecutor.executeInUnitOfWork(application.unitOfWorkManager(), () -> {
                ProcessInstance<$Type$> pi = process.instances()
                        .findById(id)
                        .orElse(null);
                if (pi == null) {
                    return null;
                } else {
                    pi.abort();
                    return getModel(pi);
                }
            });
        });
    }
    
    @POST()
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public CompletionStage<$Type$> updateModel_$name$(@PathParam("id") String id, $Type$ resource) {
        return CompletableFuture.supplyAsync(() -> {
            return org.kie.kogito.services.uow.UnitOfWorkExecutor.executeInUnitOfWork(application.unitOfWorkManager(), () -> {
                ProcessInstance<$Type$> pi = process.instances()
                        .findById(id)
                        .orElse(null);
                if (pi == null) {
                    return null;
                } else {
                    pi.updateVariables(resource);
                    return pi.variables();
                }
            });
        });
    }
    
    @GET()
    @Path("/{id}/tasks")
    @Produces(MediaType.APPLICATION_JSON)
    public CompletionStage<Map<String, String>> getTasks_$name$(@PathParam("id") String id, @QueryParam("user") final String user, @QueryParam("group") final List<String> groups) {
        return CompletableFuture.supplyAsync(() -> {
            return process.instances()
                    .findById(id)
                    .map(pi -> pi.workItems(policies(user, groups)))
                    .map(l -> l.stream().collect(Collectors.toMap(WorkItem::getId, WorkItem::getName)))
                    .orElse(null);
        });
    }
    
    protected $Type$ getModel(ProcessInstance<$Type$> pi) {
        if (pi.status() == ProcessInstance.STATE_ERROR && pi.error().isPresent()) {
            throw new ProcessInstanceExecutionException(pi.id(), pi.error().get().failedNodeId(), pi.error().get().errorMessage());
        }
        
        return pi.variables();
    }
    
    protected Policy[] policies(String user, List<String> groups) {
        if (user == null) {
            return new Policy[0];
        } 
        org.kie.kogito.auth.IdentityProvider identity = null;
        if (user != null) {
            identity = new org.kie.kogito.services.identity.StaticIdentityProvider(user, groups);
        }
        return new Policy[] {SecurityPolicy.of(identity)};
    }
}
