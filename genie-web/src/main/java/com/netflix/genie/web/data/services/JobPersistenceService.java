/*
 *
 *  Copyright 2015 Netflix, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */
package com.netflix.genie.web.data.services;

import com.netflix.genie.common.dto.Job;
import com.netflix.genie.common.dto.JobExecution;
import com.netflix.genie.common.dto.JobStatus;
import com.netflix.genie.common.exceptions.GenieException;
import com.netflix.genie.common.exceptions.GenieNotFoundException;
import com.netflix.genie.common.internal.dto.v4.AgentClientMetadata;
import com.netflix.genie.common.internal.dto.v4.JobRequest;
import com.netflix.genie.common.internal.dto.v4.JobSpecification;
import com.netflix.genie.common.internal.exceptions.unchecked.GenieApplicationNotFoundException;
import com.netflix.genie.common.internal.exceptions.unchecked.GenieClusterNotFoundException;
import com.netflix.genie.common.internal.exceptions.unchecked.GenieCommandNotFoundException;
import com.netflix.genie.common.internal.exceptions.unchecked.GenieInvalidStatusException;
import com.netflix.genie.common.internal.exceptions.unchecked.GenieJobAlreadyClaimedException;
import com.netflix.genie.common.internal.exceptions.unchecked.GenieJobNotFoundException;
import com.netflix.genie.common.internal.exceptions.unchecked.GenieRuntimeException;
import com.netflix.genie.web.dtos.JobSubmission;
import com.netflix.genie.web.dtos.ResolvedJob;
import com.netflix.genie.web.exceptions.checked.IdAlreadyExistsException;
import com.netflix.genie.web.exceptions.checked.SaveAttachmentException;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Interfaces for providing persistence functions for jobs other than search.
 *
 * @author tgianos
 * @since 3.0.0
 */
@Validated
public interface JobPersistenceService {

    /**
     * Save all the initial job fields in the data store.
     *
     * @param jobRequest   the Job request object to save. Not null
     * @param jobMetadata  metadata about the job request. Not null
     * @param job          The Job object to create
     * @param jobExecution The job execution object to create
     * @throws GenieException if there is an error
     */
    void createJob(
        @NotNull com.netflix.genie.common.dto.JobRequest jobRequest,
        @NotNull com.netflix.genie.common.dto.JobMetadata jobMetadata,
        @NotNull Job job,
        @NotNull JobExecution jobExecution
    ) throws GenieException;

    /**
     * Update the job with the various resources used to run the job including the cluster, command and applications.
     *
     * @param jobId          The id of the job to update
     * @param clusterId      The id of the cluster the job runs on
     * @param commandId      The id of the command the job runs with
     * @param applicationIds The ids of the applications used to run the job
     * @param memory         The amount of memory (in MB) to run the job with
     * @throws GenieException For any problems while updating
     */
    void updateJobWithRuntimeEnvironment(
        @NotBlank String jobId,
        @NotBlank String clusterId,
        @NotBlank String commandId,
        @NotNull List<String> applicationIds,
        @Min(1) int memory
    ) throws GenieException;

    /**
     * Update the status and status message of the job.
     *
     * @param id        The id of the job to update the status for.
     * @param jobStatus The updated status of the job.
     * @param statusMsg The updated status message of the job.
     * @throws GenieException if there is an error
     */
    void updateJobStatus(
        @NotBlank(message = "No job id entered. Unable to update.") String id,
        @NotNull(message = "Status cannot be null.") JobStatus jobStatus,
        @NotBlank(message = "Status message cannot be empty.") String statusMsg
    ) throws GenieException;

    /**
     * Update the job with information for the running job process.
     *
     * @param id         the id of the job to update the process id for
     * @param processId  The id of the process on the box for this job
     * @param checkDelay The delay to check the process with
     * @param timeout    The date at which this job should timeout
     * @throws GenieException if there is an error
     */
    void setJobRunningInformation(
        @NotBlank String id,
        @Min(value = 0, message = "Must be no lower than zero") int processId,
        @Min(value = 1, message = "Must be at least 1 millisecond, preferably much more") long checkDelay,
        @NotNull Instant timeout
    ) throws GenieException;

    /**
     * Method to set all job completion information for a job execution.
     *
     * @param id            the id of the job to update the exit code
     * @param exitCode      The exit code of the process
     * @param status        The job status for the job
     * @param statusMessage The job status message
     * @param stdOutSize    The size (in bytes) of the standard out file or null if there isn't one
     * @param stdErrSize    The size (in bytes) of the standard error file or null if there isn't one
     * @throws GenieException if there is an error
     */
    void setJobCompletionInformation(
        @NotBlank(message = "No job id entered. Unable to update.") String id,
        int exitCode,
        @NotNull(message = "No job status entered. Unable to update") JobStatus status,
        @NotBlank(message = "Status message can't be blank. Unable to update") String statusMessage,
        @Nullable Long stdOutSize,
        @Nullable Long stdErrSize
    ) throws GenieException;

    /**
     * This method will delete a chunk of jobs whose creation time is earlier than the given date.
     *
     * @param date       The date before which all jobs should be deleted
     * @param maxDeleted The maximum number of jobs that should be deleted
     *                   (soft limit, can be rounded up to multiple of page size)
     * @param pageSize   Page size used to iterate through jobs
     * @return the number of deleted jobs
     */
    long deleteBatchOfJobsCreatedBeforeDate(
        @NotNull Instant date,
        @Min(1) int maxDeleted,
        @Min(1) int pageSize
    );

    // V4 APIs

    /**
     * Save the given job submission information in the underlying data store.
     * <p>
     * The job record will be created with initial state of {@link JobStatus#RESERVED} meaning that the unique id
     * returned by this API has been reserved within Genie and no other job can use it. If
     * {@link JobSubmission} contains some attachments these attachments will be persisted to a
     * configured storage system (i.e. local disk, S3, etc) and added to the set of dependencies for the job.
     * The underlying attachment storage system must be accessible by the agent process configured by the system. For
     * example if the server is set up to write attachments to local disk but the agent is not running locally but
     * instead on the remote system it will not be able to access those attachments (as dependencies) and fail.
     * See {@link com.netflix.genie.web.services.AttachmentService} for more information.
     *
     * @param jobSubmission All the information the system has gathered regarding the job submission from the user
     *                      either via the API or via the agent CLI
     * @return The unique id of the job within the Genie ecosystem
     * @throws IdAlreadyExistsException If the id the user requested already exists in the system for another job
     * @throws SaveAttachmentException  If attachments were sent in with the job submission and they were unable to be
     *                                  persisted to an underlying storage mechanism meant to share the data with an
     *                                  agent process
     */
    @Nonnull
    String saveJobSubmission(@Valid JobSubmission jobSubmission) throws
        IdAlreadyExistsException,
        SaveAttachmentException;

    /**
     * Get the original request for a job.
     *
     * @param id The unique id of the job to get
     * @return The job request if one was found. Wrapped in {@link Optional} so empty optional returned if no job found
     * @throws GenieRuntimeException On error converting entity values to DTO values
     */
    Optional<JobRequest> getJobRequest(@NotBlank(message = "Id is missing and is required") String id);

    /**
     * Save the given resolved details for a job. Sets the job status to {@link JobStatus#RESOLVED}.
     *
     * @param id          The id of the job
     * @param resolvedJob The resolved information for the job
     * @throws GenieJobNotFoundException         When the job identified by {@code id} can't be found and the
     *                                           specification can't be saved
     * @throws GenieClusterNotFoundException     When the cluster specified in the job specification doesn't actually
     *                                           exist
     * @throws GenieCommandNotFoundException     When the command specified in the job specification doesn't actually
     *                                           exist
     * @throws GenieApplicationNotFoundException When an application specified in the job specification doesn't
     *                                           actually exist
     */
    void saveResolvedJob(
        @NotBlank(message = "Id is missing and is required") String id,
        @Valid ResolvedJob resolvedJob
    );

    /**
     * Get the saved job specification for the given job. If the job hasn't had a job specification resolved an empty
     * {@link Optional} will be returned.
     *
     * @param id The id of the job
     * @return The job specification if one is present else an empty {@link Optional}
     * @throws GenieJobNotFoundException     If no job with {@code id} exists
     * @throws GenieClusterNotFoundException When the cluster isn't found in the database which it should be at this
     *                                       point given the input to the db was valid at the time of persistence
     * @throws GenieCommandNotFoundException When the command isn't found in the database which it should be at this
     *                                       point given the input to the db was valid at the time of persistence
     * @throws GenieRuntimeException         on unexpected error
     */
    Optional<JobSpecification> getJobSpecification(
        @NotBlank(message = "Id is missing and is required") String id
    );

    /**
     * Set a job identified by {@code id} to be owned by the agent identified by {@code agentClientMetadata}. The
     * job status in the system will be set to {@link com.netflix.genie.common.dto.JobStatus#CLAIMED}
     *
     * @param id                  The id of the job to claim. Must exist in the system.
     * @param agentClientMetadata The metadata about the client claiming the job
     * @throws GenieJobNotFoundException       if no job with the given {@code id} exists
     * @throws GenieJobAlreadyClaimedException if the job with the given {@code id} already has been claimed
     * @throws GenieInvalidStatusException     if the current job status is not
     *                                         {@link com.netflix.genie.common.dto.JobStatus#RESOLVED}
     */
    void claimJob(
        @NotBlank(message = "Job id is missing and is required") String id,
        @Valid AgentClientMetadata agentClientMetadata
    );

    /**
     * Update the status of the job identified with {@code id} to be {@code newStatus} provided that the current status
     * of the job matches {@code newStatus}. Optionally a status message can be provided to provide more details to
     * users. If the {@code newStatus} is {@link JobStatus#RUNNING} the start time will be set. If the {@code newStatus}
     * is a member of {@link JobStatus#getFinishedStatuses()} and the job had a started time set the finished time of
     * the job will be set.
     *
     * @param id               The id of the job to update status for. Must exist in the system.
     * @param currentStatus    The status the caller to this API thinks the job currently has
     * @param newStatus        The new status the caller would like to update the status to
     * @param newStatusMessage An optional status message to associate with this change
     * @throws GenieJobNotFoundException   if no job with the given {@code id} exists
     * @throws GenieInvalidStatusException if the current status of the job identified by {@code id} in the system
     *                                     doesn't match the supplied {@code currentStatus}.
     *                                     Also if the {@code currentStatus} equals the {@code newStatus}.
     */
    void updateJobStatus(
        @NotBlank(message = "Id is missing and is required") String id,
        @NotNull JobStatus currentStatus,
        @NotNull JobStatus newStatus,
        @Nullable String newStatusMessage
    );

    /**
     * Get whether the job is a v4 job.
     *
     * @param id The id of the job
     * @return true if its a v4 job
     * @throws GenieJobNotFoundException If no job with {@code id} exists
     */
    boolean isV4(
        @NotBlank(message = "Id is missing and is required") String id
    );

    /**
     * Get the status for a job with the given {@code id}.
     *
     * @param id The id of the job to get status for
     * @return The job status
     * @throws GenieNotFoundException If no job with the given {@code id} exists
     */
    JobStatus getJobStatus(
        @NotBlank(message = "Job id is missing and is required") String id
    ) throws GenieNotFoundException;

    /**
     * Get the location a job directory was archived to if at all.
     *
     * @param id The id of the job to get the location for
     * @return The archive location or {@link Optional#empty()}
     * @throws GenieNotFoundException When there is no job with id {@code id}
     */
    Optional<String> getJobArchiveLocation(
        @NotBlank(message = "Job id is missing and is required") String id
    ) throws GenieNotFoundException;
}
