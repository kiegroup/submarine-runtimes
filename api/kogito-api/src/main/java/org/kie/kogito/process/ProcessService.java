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
package org.kie.kogito.process;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.kie.kogito.MapOutput;
import org.kie.kogito.Model;
import org.kie.kogito.process.workitem.Attachment;
import org.kie.kogito.process.workitem.AttachmentInfo;
import org.kie.kogito.process.workitem.Comment;
import org.kie.kogito.process.workitem.TaskModel;

public interface ProcessService {

    ProcessInstance createProcessInstance(Process process, String businessKey,
            Model model,
            String startFromNodeId);

    <T extends Model> List<T> getProcessInstanceOutput(Process process, Class<T> modelType);

    <T extends Model> Optional<T> findById(Process process, String id, Class<T> modelType);

    <T extends Model> Optional<T> delete(Process process, String id, Class<T> modelType);

    <T extends Model> Optional<T> update(Process process, String id, Model resource, Class<T> modelType);

    Optional<List<WorkItem>> getTasks(Process process, String id, String user, List<String> groups);

    Optional<WorkItem> signalTask(Process process, String id, String taskNodeName, String taskName);

    Optional<WorkItem> getTaskByName(ProcessInstance pi, String taskName);

    <T extends Model> Optional<T> completeTask(Process process,
            String id,
            String taskId,
            String phase,
            String user,
            List<String> groups,
            MapOutput taskModel,
            Class<T> modelType);

    <T extends MapOutput> Optional<T> saveTask(Process process,
            String id,
            String taskId,
            String user,
            List<String> groups,
            T model,
            Function<Map<String, Object>, T> mapper);

    <T extends Model> Optional<T> taskTransition(
            Process process,
            String id,
            String taskId,
            String phase,
            String user,
            List<String> groups,
            MapOutput model,
            Class<T> modelType);

    <T extends TaskModel<?, ?>> Optional<T> getTask(Process process,
            String id,
            String taskId,
            String user,
            List<String> groups,
            Function<WorkItem, T> mapper);

    <T extends Model> Optional<T> abortTask(Process process,
            String id,
            String taskId,
            String phase,
            String user,
            List<String> groups,
            Class<T> modelType);

    Optional<Comment> addComment(Process process,
            String id,
            String taskId,
            String user,
            List<String> groups,
            String commentInfo);

    Optional<Comment> updateComment(Process process,
            String id,
            String taskId,
            String commentId,
            String user,
            List<String> groups,
            String commentInfo);

    Optional<Boolean> deleteComment(Process process,
            String id,
            String taskId,
            String commentId,
            String user,
            List<String> groups);

    Optional<Attachment> addAttachment(Process process,
            String id,
            String taskId,
            String user,
            List<String> groups,
            AttachmentInfo attachmentInfo);

    Optional<Attachment> updateAttachment(Process process,
            String id,
            String taskId,
            String attachmentId,
            String user,
            List<String> groups,
            AttachmentInfo attachment);

    Optional<Boolean> deleteAttachment(Process process,
            String id,
            String taskId,
            String attachmentId,
            String user,
            List<String> groups);

    Optional<Attachment> getAttachment(Process process,
            String id,
            String taskId,
            String attachmentId,
            String user,
            List<String> groups);

    Optional<Collection<Attachment>> getAttachments(Process process,
            String id,
            String taskId,
            String user,
            List<String> groups);

    Optional<Comment> getComment(Process process,
            String id,
            String taskId,
            String commentId,
            String user,
            List<String> groups);

    Optional<Collection<Comment>> getComments(Process process,
            String id,
            String taskId,
            String user,
            List<String> groups);

    <T extends Model> Optional<T> signalProcessInstance(Process process, String id, Object data, String signalName, Class<T> modelType);

    //Schema
    Map<String, Object> getSchemaAndPhases(Process process,
            String id,
            String taskId,
            String user,
            List<String> groups);
}
