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
package com.netflix.genie.web.data.services.jpa;

import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import com.google.common.collect.Sets;
import com.netflix.genie.common.dto.Application;
import com.netflix.genie.common.dto.Job;
import com.netflix.genie.common.dto.JobMetadata;
import com.netflix.genie.common.dto.JobRequest;
import com.netflix.genie.common.dto.JobStatus;
import com.netflix.genie.common.dto.UserResourcesSummary;
import com.netflix.genie.common.dto.search.JobSearchResult;
import com.netflix.genie.common.exceptions.GenieException;
import com.netflix.genie.common.exceptions.GenieNotFoundException;
import com.netflix.genie.test.suppliers.RandomSuppliers;
import com.netflix.genie.web.data.services.JobSearchService;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.Lists;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Integration tests for the Job Search Service using JPA.
 *
 * @author tgianos
 * @since 3.0.0
 */
@DatabaseSetup("JpaJobSearchServiceImplIntegrationTest/init.xml")
@DatabaseTearDown("cleanup.xml")
public class JpaJobSearchServiceImplIntegrationTest extends DBIntegrationTestBase {

    private static final String JOB_1_ID = "job1";
    private static final String JOB_2_ID = "job2";
    private static final String JOB_3_ID = "job3";

    // This needs to be injected as a Spring Bean otherwise transactions don't work as there is no proxy
    @Autowired
    private JobSearchService service;

    /**
     * Make sure we can search jobs successfully.
     */
    @Test
    public void canFindJobs() {
        //TODO: add more cases
        final Pageable page = PageRequest.of(0, 10, Sort.Direction.DESC, "updated");
        Page<JobSearchResult> jobs = this.service
            .findJobs(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                page
            );
        Assert.assertThat(jobs.getTotalElements(), Matchers.is(5L));

        jobs = this.service
            .findJobs(
                null,
                null,
                null,
                Sets.newHashSet(JobStatus.RUNNING),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                page
            );
        Assert.assertThat(jobs.getTotalElements(), Matchers.is(3L));
        Assert.assertThat(
            jobs
                .getContent()
                .stream()
                .filter(job -> job.getId().equals(JOB_3_ID))
                .count(),
            Matchers.is(1L)
        );

        jobs = this.service
            .findJobs(
                JOB_1_ID,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                page
            );
        Assert.assertThat(jobs.getTotalElements(), Matchers.is(1L));
        Assert.assertThat(
            jobs
                .getContent()
                .stream()
                .filter(job -> job.getId().equals(JOB_1_ID))
                .count(),
            Matchers.is(1L)
        );

        jobs = this.service
            .findJobs(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "job3Grouping",
                null,
                page
            );
        Assert.assertThat(jobs.getTotalElements(), Matchers.is(1L));
        Assert.assertThat(
            jobs
                .getContent()
                .stream()
                .filter(job -> job.getId().equals(JOB_3_ID))
                .count(),
            Matchers.is(1L)
        );

        jobs = this.service
            .findJobs(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "job2%",
                page
            );
        Assert.assertThat(jobs.getTotalElements(), Matchers.is(1L));
        Assert.assertThat(
            jobs
                .getContent()
                .stream()
                .filter(job -> job.getId().equals(JOB_2_ID))
                .count(),
            Matchers.is(1L)
        );

        jobs = this.service
            .findJobs(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "job1%",
                "job2%",
                page
            );
        Assert.assertThat(jobs.getTotalElements(), Matchers.is(0L));
        Assert.assertTrue(jobs.getContent().isEmpty());
    }

    /**
     * Specific searches for jobs by cluster or command variables which behave special due to joins.
     */
    @Test
    public void canFindJobsByClusterAndCommand() {
        final String clusterId = "cluster1";
        final String clusterName = "h2query";
        final String commandId = "command1";
        final String commandName = "spark";
        final Pageable page = PageRequest.of(0, 10, Sort.Direction.DESC, "updated");
        Page<JobSearchResult> jobs = this.service
            .findJobs(
                null,
                null,
                null,
                null,
                null,
                null,
                UUID.randomUUID().toString(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                page
            );
        Assert.assertThat(jobs.getTotalElements(), Matchers.is(0L));
        Assert.assertTrue(jobs.getContent().isEmpty());

        jobs = this.service
            .findJobs(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                UUID.randomUUID().toString(),
                null,
                null,
                null,
                null,
                null,
                null,
                page
            );
        Assert.assertThat(jobs.getTotalElements(), Matchers.is(0L));
        Assert.assertTrue(jobs.getContent().isEmpty());

        jobs = this.service
            .findJobs(
                null,
                null,
                null,
                null,
                null,
                null,
                clusterId,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                page
            );
        Assert.assertThat(jobs.getTotalElements(), Matchers.is(5L));
        Assert.assertThat(jobs.getContent().size(), Matchers.is(5));

        jobs = this.service
            .findJobs(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                commandId,
                null,
                null,
                null,
                null,
                null,
                null,
                page
            );
        Assert.assertThat(jobs.getTotalElements(), Matchers.is(5L));
        Assert.assertThat(jobs.getContent().size(), Matchers.is(5));

        jobs = this.service
            .findJobs(
                null,
                null,
                null,
                null,
                null,
                null,
                clusterId,
                null,
                commandId,
                null,
                null,
                null,
                null,
                null,
                null,
                page
            );
        Assert.assertThat(jobs.getTotalElements(), Matchers.is(5L));
        Assert.assertThat(jobs.getContent().size(), Matchers.is(5));

        jobs = this.service
            .findJobs(
                null,
                null,
                null,
                null,
                null,
                clusterName,
                clusterId,
                commandName,
                commandId,
                null,
                null,
                null,
                null,
                null,
                null,
                page
            );
        Assert.assertThat(jobs.getTotalElements(), Matchers.is(5L));
        Assert.assertThat(jobs.getContent().size(), Matchers.is(5));

        jobs = this.service
            .findJobs(
                null,
                null,
                null,
                null,
                null,
                UUID.randomUUID().toString(),
                clusterId,
                commandName,
                commandId,
                null,
                null,
                null,
                null,
                null,
                null,
                page
            );
        Assert.assertThat(jobs.getTotalElements(), Matchers.is(0L));
        Assert.assertTrue(jobs.getContent().isEmpty());

        jobs = this.service
            .findJobs(
                null,
                null,
                null,
                null,
                null,
                clusterName,
                clusterId,
                UUID.randomUUID().toString(),
                commandId,
                null,
                null,
                null,
                null,
                null,
                null,
                page
            );
        Assert.assertThat(jobs.getTotalElements(), Matchers.is(0L));
        Assert.assertTrue(jobs.getContent().isEmpty());
    }

    /**
     * Make sure we can get the correct number of jobs which are active on a given host.
     */
    @Test
    public void canFindActiveJobsByHostName() {
        final String hostA = "a.netflix.com";
        final String hostB = "b.netflix.com";
        final String hostC = "c.netflix.com";

        Set<Job> jobs = this.service.getAllActiveJobsOnHost(hostA);
        Assert.assertThat(jobs.size(), Matchers.is(1));
        Assert.assertThat(
            jobs
                .stream()
                .filter(
                    jobExecution ->
                        JOB_2_ID.equals(jobExecution.getId().orElseThrow(IllegalArgumentException::new))
                )
                .count(),
            Matchers.is(1L)
        );

        jobs = this.service.getAllActiveJobsOnHost(hostB);
        Assert.assertThat(jobs.size(), Matchers.is(1));
        Assert.assertThat(
            jobs
                .stream()
                .filter(
                    jobExecution -> JOB_3_ID.equals(jobExecution.getId().orElseThrow(IllegalArgumentException::new))
                )
                .count(),
            Matchers.is(1L)
        );

        jobs = this.service.getAllActiveJobsOnHost(hostC);
        Assert.assertTrue(jobs.isEmpty());
    }

    /**
     * Make sure we can get the host names of nodes currently running jobs.
     */
    @Test
    public void canFindHostNamesOfActiveJobs() {
        final String hostA = "a.netflix.com";
        final String hostB = "b.netflix.com";

        final Set<String> hostNames = this.service.getAllHostsWithActiveJobs();
        Assert.assertThat(hostNames.size(), Matchers.is(2));
        Assert.assertThat(hostNames, Matchers.hasItems(hostA, hostB));
    }

    /**
     * Make sure the getting job method works.
     *
     * @throws GenieException on error
     */
    @Test
    public void canGetJob() throws GenieException {
        Assert.assertThat(this.service.getJob(JOB_1_ID).getName(), Matchers.is("testSparkJob"));
        Assert.assertThat(this.service.getJob(JOB_2_ID).getName(), Matchers.is("testSparkJob1"));
        Assert.assertThat(this.service.getJob(JOB_3_ID).getName(), Matchers.is("testSparkJob2"));

        try {
            this.service.getJobStatus(UUID.randomUUID().toString());
            Assert.fail();
        } catch (final GenieException ge) {
            Assert.assertTrue(ge instanceof GenieNotFoundException);
        }
    }

    /**
     * Make sure the job status method works.
     *
     * @throws GenieException on error
     */
    @Test
    public void canGetJobStatus() throws GenieException {
        Assert.assertThat(this.service.getJobStatus(JOB_1_ID), Matchers.is(JobStatus.SUCCEEDED));
        Assert.assertThat(this.service.getJobStatus(JOB_2_ID), Matchers.is(JobStatus.INIT));
        Assert.assertThat(this.service.getJobStatus(JOB_3_ID), Matchers.is(JobStatus.RUNNING));

        try {
            this.service.getJobStatus(UUID.randomUUID().toString());
            Assert.fail();
        } catch (final GenieException ge) {
            Assert.assertTrue(ge instanceof GenieNotFoundException);
        }
    }

    /**
     * Make sure the getting job request method works.
     *
     * @throws GenieException on error
     */
    @Test
    public void canGetJobRequest() throws GenieException {
        final JobRequest job1Request = this.service.getJobRequest(JOB_1_ID);
        Assert.assertThat(
            job1Request.getCommandArgsString().orElseThrow(IllegalArgumentException::new),
            Matchers.is("'-f' 'query.q'")
        );
        Assert.assertThat(job1Request.getCommandArgs(), Matchers.is(Lists.newArrayList("-f", "query.q")));
        Assert.assertThat(
            job1Request.getClusterCriterias().size(),
            Matchers.is(2)
        );
        final Set<String> clusterCriterion0Tags = job1Request.getClusterCriterias().get(0).getTags();
        Assert.assertThat(
            clusterCriterion0Tags.size(),
            Matchers.is(4)
        );
        Assert.assertThat(
            clusterCriterion0Tags,
            Matchers.is(Sets.newHashSet("genie.id:cluster1", "genie.name:h2query", "sla", "yarn"))
        );
        final Set<String> clusterCriterion1Tags = job1Request.getClusterCriterias().get(1).getTags();
        Assert.assertThat(
            clusterCriterion1Tags.size(),
            Matchers.is(2)
        );
        Assert.assertThat(
            clusterCriterion1Tags,
            Matchers.is(Sets.newHashSet("sla", "adhoc"))
        );
        final Set<String> commandCriterionTags = job1Request.getCommandCriteria();
        Assert.assertThat(
            commandCriterionTags,
            Matchers.is(Sets.newHashSet("type:spark", "ver:1.6.0", "genie.name:spark"))
        );
        Assert.assertThat(
            this.service.getJobRequest(JOB_2_ID).getCommandArgsString().orElseThrow(IllegalArgumentException::new),
            Matchers.is("'-f' 'spark.jar'")
        );
        Assert.assertThat(
            this.service.getJobRequest(JOB_3_ID).getCommandArgsString().orElseThrow(IllegalArgumentException::new),
            Matchers.is("'-f' 'spark.jar'")
        );

        try {
            this.service.getJobRequest(UUID.randomUUID().toString());
            Assert.fail();
        } catch (final GenieException ge) {
            Assert.assertTrue(ge instanceof GenieNotFoundException);
        }
    }

    /**
     * Make sure the getting job execution method works.
     *
     * @throws GenieException on error
     */
    @Test
    public void canGetJobExecution() throws GenieException {
        Assert.assertThat(
            this.service.getJobExecution(JOB_1_ID).getProcessId().orElseThrow(IllegalArgumentException::new),
            Matchers.is(317)
        );
        Assert.assertThat(
            this.service.getJobExecution(JOB_2_ID).getProcessId().orElseThrow(IllegalArgumentException::new),
            Matchers.is(318)
        );
        Assert.assertThat(
            this.service.getJobExecution(JOB_3_ID).getProcessId().orElseThrow(IllegalArgumentException::new),
            Matchers.is(319)
        );

        try {
            this.service.getJobExecution(UUID.randomUUID().toString());
            Assert.fail();
        } catch (final GenieException ge) {
            Assert.assertTrue(ge instanceof GenieNotFoundException);
        }
    }

    /**
     * Make sure getting the job cluster method returns a valid cluster.
     *
     * @throws GenieException on error
     */
    @Test
    public void canGetJobCluster() throws GenieException {
        Assert.assertThat(
            this.service.getJobCluster(JOB_1_ID).getId().orElseThrow(IllegalArgumentException::new),
            Matchers.is("cluster1")
        );
    }

    /**
     * Make sure getting the job command method returns a valid command.
     *
     * @throws GenieException on error
     */
    @Test
    public void canGetJobCommand() throws GenieException {
        Assert.assertThat(
            this.service.getJobCommand(JOB_1_ID).getId().orElseThrow(IllegalArgumentException::new),
            Matchers.is("command1")
        );
    }

    /**
     * Make sure getting the job applications method returns a valid list of applications.
     *
     * @throws GenieException on error
     */
    @Test
    public void canGetJobApplications() throws GenieException {
        List<Application> applications = this.service.getJobApplications(JOB_1_ID);
        Assert.assertThat(applications.size(), Matchers.is(2));
        Assert.assertThat(applications.get(0).getId().orElseGet(RandomSuppliers.STRING), Matchers.is("app1"));
        Assert.assertThat(applications.get(1).getId().orElseGet(RandomSuppliers.STRING), Matchers.is("app3"));
        applications = this.service.getJobApplications(JOB_2_ID);
        Assert.assertThat(applications.size(), Matchers.is(2));
        Assert.assertThat(applications.get(0).getId().orElseGet(RandomSuppliers.STRING), Matchers.is("app1"));
        Assert.assertThat(applications.get(1).getId().orElseGet(RandomSuppliers.STRING), Matchers.is("app2"));
    }

    /**
     * Make sure we can get the correct number of jobs which are active for a given user.
     *
     * @throws GenieException on error
     */
    @Test
    public void canGetActiveJobCountForUser() throws GenieException {
        Assert.assertThat(this.service.getActiveJobCountForUser("nobody"), Matchers.is(0L));
        Assert.assertThat(this.service.getActiveJobCountForUser("tgianos"), Matchers.is(4L));
    }

    /**
     * Make sure the getting job execution method works.
     *
     * @throws GenieException on error
     */
    @Test
    public void canGetJobMetadata() throws GenieException {
        final JobMetadata jobMetadata = this.service.getJobMetadata(JOB_1_ID);
        Assert.assertThat(jobMetadata.getClientHost(), Matchers.is(Optional.empty()));
        Assert.assertThat(jobMetadata.getUserAgent(), Matchers.is(Optional.empty()));
        Assert.assertThat(jobMetadata.getNumAttachments(), Matchers.is(Optional.of(2)));
        Assert.assertThat(jobMetadata.getTotalSizeOfAttachments(), Matchers.is(Optional.of(38083L)));
        Assert.assertThat(jobMetadata.getStdErrSize(), Matchers.is(Optional.empty()));
        Assert.assertThat(jobMetadata.getStdOutSize(), Matchers.is(Optional.empty()));
    }

    /**
     * Make sure the getting user resource summaries works.
     */
    @Test
    public void canGetUserResourceSummaries() {
        final Map<String, UserResourcesSummary> summaries = this.service.getUserResourcesSummaries();
        Assert.assertThat(summaries.keySet(), Matchers.contains("tgianos"));
        final UserResourcesSummary userResourcesSummary = summaries.get("tgianos");
        Assert.assertThat(userResourcesSummary.getUser(), Matchers.is("tgianos"));
        Assert.assertThat(userResourcesSummary.getRunningJobsCount(), Matchers.is(1L));
        Assert.assertThat(userResourcesSummary.getUsedMemory(), Matchers.is(2048L));
    }

    /**
     * Make sure the getting active agent jobs without an active connection works.
     */
    @Test
    public void canGetActiveDisconnectedAgentJobs() {
        final Set<String> jobs = this.service.getActiveDisconnectedAgentJobs();
        Assert.assertThat(jobs.size(), Matchers.is(1));
        Assert.assertThat(jobs, Matchers.contains("agentJob1"));
    }

    /**
     * Make sure the amount of allocated memory for active jobs is correct.
     */
    @Test
    public void canGetAllocatedMemoryOnHost() {
        Assertions.assertThat(this.service.getAllocatedMemoryOnHost("a.netflix.com")).isEqualTo(2048L);
        Assertions.assertThat(this.service.getAllocatedMemoryOnHost("b.netflix.com")).isEqualTo(2048L);
        Assertions.assertThat(this.service.getAllocatedMemoryOnHost("agent.netflix.com")).isEqualTo(4096L);
        Assertions.assertThat(this.service.getAllocatedMemoryOnHost(UUID.randomUUID().toString())).isEqualTo(0L);
    }

    /**
     * Make sure the amount of used memory for running jobs is correct.
     */
    @Test
    public void canGetUsedMemoryOnHost() {
        Assertions.assertThat(this.service.getUsedMemoryOnHost("a.netflix.com")).isEqualTo(2048L);
        Assertions.assertThat(this.service.getUsedMemoryOnHost("b.netflix.com")).isEqualTo(2048L);
        Assertions.assertThat(this.service.getUsedMemoryOnHost("agent.netflix.com")).isEqualTo(4096L);
        Assertions.assertThat(this.service.getUsedMemoryOnHost(UUID.randomUUID().toString())).isEqualTo(0L);
    }

    /**
     * Make sure the amount of used memory for active jobs is correct.
     */
    @Test
    public void canGetCountOfActiveJobsOnHost() {
        Assertions.assertThat(this.service.getActiveJobCountOnHost("a.netflix.com")).isEqualTo(1L);
        Assertions.assertThat(this.service.getActiveJobCountOnHost("b.netflix.com")).isEqualTo(1L);
        Assertions.assertThat(this.service.getActiveJobCountOnHost("agent.netflix.com")).isEqualTo(2L);
        Assertions.assertThat(this.service.getActiveJobCountOnHost(UUID.randomUUID().toString())).isEqualTo(0L);
    }
}
