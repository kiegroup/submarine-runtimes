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
package org.kie.kogito.process.impl;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jbpm.process.instance.impl.humantask.HumanTaskHelper;
import org.jbpm.process.instance.impl.humantask.HumanTaskTransition;
import org.jbpm.util.JsonSchemaUtil;
import org.kie.kogito.Application;
import org.kie.kogito.MapOutput;
import org.kie.kogito.MappableToModel;
import org.kie.kogito.Model;
import org.kie.kogito.process.*;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.workitem.*;
import org.kie.kogito.services.uow.UnitOfWorkExecutor;

public class ProcessServiceImpl implements ProcessService {

    private final Application application;

    public ProcessServiceImpl(Application application) {
        this.application = application;
    }

    @Override
    public ProcessInstance createProcessInstance(Process process, String businessKey,
            Model model,
            String startFromNodeId) {
        return UnitOfWorkExecutor.executeInUnitOfWork(application.unitOfWorkManager(), () -> {
            ProcessInstance pi = process.createInstance(businessKey, model);
            if (startFromNodeId != null) {
                pi.startFrom(startFromNodeId);
            } else {
                pi.start();
            }
            return pi;
        });
    }

    @Override
    public <T extends Model> List<T> getProcessInstanceOutput(Process process, Class<T> modelType) {
        return process.instances().values().stream()
                .map(ProcessInstance::variables)
                .map(MappableToModel.class::cast)
                .map(MappableToModel::toModel)
                .map(modelType::cast)
                .collect(Collectors.toList());
    }

    @Override
    public <T extends Model> Optional<T> findById(Process process, String id, Class<T> modelType) {
        return process.instances()
                .findById(id, ProcessInstanceReadMode.READ_ONLY)
                .map(ProcessInstance::variables)
                .map(MappableToModel.class::cast)
                .map(MappableToModel::toModel)
                .map(modelType::cast);
    }

    @Override
    public <T extends Model> Optional<T> delete(Process process, String id, Class<T> modelType) {
        return UnitOfWorkExecutor.executeInUnitOfWork(
                application.unitOfWorkManager(),
                () -> process
                        .instances()
                        .findById(id)
                        .map(pi -> {
                            pi.abort();
                            return pi;
                        })
                        .map(ProcessInstance::checkError)
                        .map(ProcessInstance::variables)
                        .map(MappableToModel.class::cast)
                        .map(MappableToModel::toModel))
                .map(modelType::cast);
    }

    @Override
    public <T extends Model> Optional<T> update(Process process, String id, Model resource, Class<T> modelType) {
        return UnitOfWorkExecutor.executeInUnitOfWork(
                application.unitOfWorkManager(),
                () -> process
                        .instances()
                        .findById(id)
                        .map(pi -> pi.updateVariables(resource))
                        .map(MappableToModel.class::cast)
                        .map(m -> (T) m.toModel()));
    }

    @Override
    public Optional<List<WorkItem>> getTasks(Process process, String id, String user, List<String> groups) {
        return process.instances()
                .findById(id, ProcessInstanceReadMode.READ_ONLY)
                .map(pi -> pi.workItems(Policies.of(user, groups)));
    }

    @Override
    public Optional<WorkItem> signalTask(Process process, String id, String taskNodeName, String taskName) {
        return UnitOfWorkExecutor.executeInUnitOfWork(application.unitOfWorkManager(), () -> process
                .instances()
                .findById(id)
                .map(pi -> {
                    pi.send(Sig.of(taskNodeName, Collections.emptyMap()));
                    return pi;
                })
                .map(pi -> getTaskByName(pi, taskName).orElse(null)));
    }

    @Override
    public Optional<WorkItem> getTaskByName(ProcessInstance pi, String taskName) {
        return pi
                .workItems()
                .stream()
                .filter(wi -> wi.getName().equals(taskName))
                .findFirst();
    }

    @Override
    public <T extends Model> Optional<T> completeTask(Process process,
            String id,
            String taskId,
            String phase,
            String user,
            List<String> groups,
            MapOutput taskModel,
            Class<T> modelType) {
        return UnitOfWorkExecutor.executeInUnitOfWork(application.unitOfWorkManager(), () -> process
                .instances()
                .findById(id)
                .map(pi -> {
                    pi.transitionWorkItem(
                            taskId,
                            HumanTaskTransition.withModel(phase, taskModel, Policies.of(user, groups)));
                    return pi;
                })
                .map(ProcessInstance::variables)
                .map(MappableToModel.class::cast)
                .map(MappableToModel::toModel)
                .map(modelType::cast));
    }

    @Override
    public <T extends MapOutput> Optional<T> saveTask(Process process,
            String id,
            String taskId,
            String user,
            List<String> groups,
            T model,
            Function<Map<String, Object>, T> mapper) {
        return UnitOfWorkExecutor.executeInUnitOfWork(application.unitOfWorkManager(), () -> process
                .instances()
                .findById(id)
                .map(pi -> pi.updateWorkItem(taskId, wi -> HumanTaskHelper.updateContent(wi, model), Policies.of(user, groups))))
                .map(mapper::apply);
    }

    @Override
    public <T extends Model> Optional<T> taskTransition(
            Process process,
            String id,
            String taskId,
            String phase,
            String user,
            List<String> groups,
            MapOutput model,
            Class<T> modelType) {
        return UnitOfWorkExecutor.executeInUnitOfWork(
                application.unitOfWorkManager(), () -> process
                        .instances()
                        .findById(id)
                        .map(pi -> {
                            pi.transitionWorkItem(
                                    taskId,
                                    HumanTaskTransition.withModel(phase, model, Policies.of(user, groups)));
                            MappableToModel variables = (MappableToModel) pi.variables();
                            return (T) variables.toModel();
                        }));
    }

    @Override
    public <T extends TaskModel<?, ?>> Optional<T> getTask(Process process,
            String id,
            String taskId,
            String user,
            List<String> groups,
            Function<WorkItem, T> mapper) {
        return process.instances()
                .findById(id, ProcessInstanceReadMode.READ_ONLY)
                .map(pi -> pi.workItem(taskId, Policies.of(user, groups)))
                .map(mapper::apply);
    }

    @Override
    public <T extends Model> Optional<T> abortTask(Process process,
            String id,
            String taskId,
            String phase,
            String user,
            List<String> groups,
            Class<T> modelType) {
        return UnitOfWorkExecutor.executeInUnitOfWork(
                application.unitOfWorkManager(), () -> process
                        .instances()
                        .findById(id)
                        .map(pi -> {
                            pi.transitionWorkItem(taskId,
                                    HumanTaskTransition.withoutModel(phase,
                                            Policies.of(user, groups)));
                            MappableToModel variables = (MappableToModel) pi.variables();
                            return (T) variables.toModel();
                        }));
    }

    @Override
    public Optional<Comment> addComment(Process process,
            String id,
            String taskId,
            String user,
            List<String> groups,
            String commentInfo) {
        return UnitOfWorkExecutor.executeInUnitOfWork(
                application.unitOfWorkManager(), () -> process
                        .instances()
                        .findById(id)
                        .map(pi -> pi.updateWorkItem(
                                taskId,
                                wi -> HumanTaskHelper.addComment(wi, commentInfo, user),
                                Policies.of(user, groups))));
    }

    @Override
    public Optional<Comment> updateComment(Process process,
            String id,
            String taskId,
            String commentId,
            String user,
            List<String> groups,
            String commentInfo) {
        return UnitOfWorkExecutor.executeInUnitOfWork(
                application.unitOfWorkManager(), () -> process
                        .instances()
                        .findById(id)
                        .map(pi -> pi.updateWorkItem(
                                taskId,
                                wi -> HumanTaskHelper.updateComment(wi, commentId, commentInfo, user),
                                Policies.of(user, groups))));
    }

    @Override
    public Optional<Boolean> deleteComment(Process process,
            String id,
            String taskId,
            String commentId,
            String user,
            List<String> groups) {
        return UnitOfWorkExecutor.executeInUnitOfWork(
                application.unitOfWorkManager(), () -> process
                        .instances()
                        .findById(id)
                        .map(pi -> pi.updateWorkItem(
                                taskId,
                                wi -> HumanTaskHelper.deleteComment(wi, commentId, user),
                                Policies.of(user, groups))));
    }

    @Override
    public Optional<Attachment> addAttachment(Process process,
            String id,
            String taskId,
            String user,
            List<String> groups,
            AttachmentInfo attachmentInfo) {
        return UnitOfWorkExecutor.executeInUnitOfWork(
                application.unitOfWorkManager(), () -> process
                        .instances()
                        .findById(id)
                        .map(pi -> pi.updateWorkItem(
                                taskId,
                                wi -> HumanTaskHelper.addAttachment(wi, attachmentInfo, user),
                                Policies.of(user, groups))));
    }

    @Override
    public Optional<Attachment> updateAttachment(Process process,
            String id,
            String taskId,
            String attachmentId,
            String user,
            List<String> groups,
            AttachmentInfo attachment) {
        return UnitOfWorkExecutor.executeInUnitOfWork(
                application.unitOfWorkManager(), () -> process
                        .instances()
                        .findById(id)
                        .map(pi -> pi.updateWorkItem(
                                taskId,
                                wi -> HumanTaskHelper.updateAttachment(wi, attachmentId, attachment, user),
                                Policies.of(user, groups))));
    }

    @Override
    public Optional<Boolean> deleteAttachment(Process process,
            String id,
            String taskId,
            String attachmentId,
            String user,
            List<String> groups) {
        return UnitOfWorkExecutor.executeInUnitOfWork(
                application.unitOfWorkManager(), () -> process
                        .instances()
                        .findById(id)
                        .map(pi -> pi.updateWorkItem(
                                taskId,
                                wi -> HumanTaskHelper.deleteAttachment(wi, attachmentId, user),
                                Policies.of(user, groups))));
    }

    @Override
    public Optional<Attachment> getAttachment(Process process,
            String id,
            String taskId,
            String attachmentId,
            String user,
            List<String> groups) {
        return process.instances().findById(id)
                .map(pi -> HumanTaskHelper.findTask(pi, taskId, Policies.of(user, groups)))
                .map(HumanTaskWorkItem::getAttachments)
                .map(attachments -> attachments.get(attachmentId));
    }

    @Override
    public Optional<Collection<Attachment>> getAttachments(Process process,
            String id,
            String taskId,
            String user,
            List<String> groups) {
        return process.instances().findById(id)
                .map(pi -> HumanTaskHelper.findTask(pi, taskId, Policies.of(user, groups)))
                .map(HumanTaskWorkItem::getAttachments)
                .map(Map::values);
    }

    @Override
    public Optional<Comment> getComment(Process process,
            String id,
            String taskId,
            String commentId,
            String user,
            List<String> groups) {
        return process.instances().findById(id)
                .map(pi -> HumanTaskHelper.findTask(pi, taskId, Policies.of(user, groups)))
                .map(HumanTaskWorkItem::getComments)
                .map(comments -> comments.get(commentId));
    }

    @Override
    public Optional<Collection<Comment>> getComments(Process process,
            String id,
            String taskId,
            String user,
            List<String> groups) {
        return process.instances().findById(id)
                .map(pi -> HumanTaskHelper.findTask(pi, taskId, Policies.of(user, groups)))
                .map(HumanTaskWorkItem::getComments)
                .map(Map::values);
    }

    @Override
    public <T extends Model> Optional<T> signalProcessInstance(Process process, String id, Object data, String signalName, Class<T> modelType) {
        return UnitOfWorkExecutor.executeInUnitOfWork(application.unitOfWorkManager(),
                () -> process.instances().findById(id)
                        .map(pi -> {
                            pi.send(Sig.of(signalName, data));
                            MappableToModel variables = (MappableToModel) pi.checkError().variables();
                            return (T) variables.toModel();
                        }));
    }

    //Schema
    @Override
    public Map<String, Object> getSchemaAndPhases(Process process,
            String id,
            String taskId,
            String user,
            List<String> groups) {
        return JsonSchemaUtil.addPhases(
                process,
                application.config().get(ProcessConfig.class).workItemHandlers().forName("Human Task"),
                id,
                taskId,
                Policies.of(user, groups),
                JsonSchemaUtil.load(this.getClass().getClassLoader(), process.id(), "$taskName$"));
    }
}
