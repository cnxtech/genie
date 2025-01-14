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
package com.netflix.genie.common.dto;

import com.google.common.collect.Sets;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Unit tests for the Job class.
 *
 * @author tgianos
 * @since 3.0.0
 */
public class JobTest {

    private static final String NAME = UUID.randomUUID().toString();
    private static final String USER = UUID.randomUUID().toString();
    private static final String VERSION = UUID.randomUUID().toString();
    private static final String COMMAND_ARG_1 = UUID.randomUUID().toString();
    private static final String COMMAND_ARG_2 = UUID.randomUUID().toString() + ' ' + UUID.randomUUID().toString();
    private static final String COMMAND_ARG_3 = UUID.randomUUID().toString();
    private static final List<String> COMMAND_ARGS = com.google.common.collect.Lists.newArrayList(
        COMMAND_ARG_1,
        COMMAND_ARG_2,
        COMMAND_ARG_3
    );
    private static final String COMMAND_ARGS_INPUT_STRING = COMMAND_ARG_1 + " '" + COMMAND_ARG_2 + "' " + COMMAND_ARG_3;
    private static final String COMMAND_ARGS_EXPECTED
        = '\'' + COMMAND_ARG_1 + "' '" + COMMAND_ARG_2 + "' '" + COMMAND_ARG_3 + '\'';

    /**
     * Test to make sure can build a valid Job using the builder.
     */
    @Test
    @SuppressWarnings("deprecation")
    public void canBuildJobDeprecatedConstructor() {
        final Job job = new Job.Builder(NAME, USER, VERSION, COMMAND_ARGS_INPUT_STRING).build();
        Assert.assertThat(job.getName(), Matchers.is(NAME));
        Assert.assertThat(job.getUser(), Matchers.is(USER));
        Assert.assertThat(job.getVersion(), Matchers.is(VERSION));
        Assert.assertThat(
            job.getCommandArgsString().orElseThrow(IllegalArgumentException::new),
            Matchers.is(COMMAND_ARGS_EXPECTED)
        );
        Assert.assertThat(job.getCommandArgs(), Matchers.is(COMMAND_ARGS));
        Assert.assertFalse(job.getArchiveLocation().isPresent());
        Assert.assertFalse(job.getClusterName().isPresent());
        Assert.assertFalse(job.getCommandName().isPresent());
        Assert.assertFalse(job.getFinished().isPresent());
        Assert.assertFalse(job.getStarted().isPresent());
        Assert.assertThat(job.getStatus(), Matchers.is(JobStatus.INIT));
        Assert.assertFalse(job.getStatusMsg().isPresent());
        Assert.assertFalse(job.getCreated().isPresent());
        Assert.assertFalse(job.getDescription().isPresent());
        Assert.assertFalse(job.getId().isPresent());
        Assert.assertThat(job.getTags(), Matchers.empty());
        Assert.assertFalse(job.getUpdated().isPresent());
        Assert.assertThat(job.getRuntime(), Matchers.is(Duration.ZERO));
        Assert.assertThat(job.getGrouping(), Matchers.is(Optional.empty()));
        Assert.assertThat(job.getGroupingInstance(), Matchers.is(Optional.empty()));
    }

    /**
     * Test to make sure can build a valid Job using the builder.
     */
    @Test
    public void canBuildJob() {
        final Job job = new Job.Builder(NAME, USER, VERSION).build();
        Assert.assertThat(job.getName(), Matchers.is(NAME));
        Assert.assertThat(job.getUser(), Matchers.is(USER));
        Assert.assertThat(job.getVersion(), Matchers.is(VERSION));
        Assert.assertThat(job.getCommandArgsString(), Matchers.is(Optional.empty()));
        Assert.assertTrue(job.getCommandArgs().isEmpty());
        Assert.assertFalse(job.getArchiveLocation().isPresent());
        Assert.assertFalse(job.getClusterName().isPresent());
        Assert.assertFalse(job.getCommandName().isPresent());
        Assert.assertFalse(job.getFinished().isPresent());
        Assert.assertFalse(job.getStarted().isPresent());
        Assert.assertThat(job.getStatus(), Matchers.is(JobStatus.INIT));
        Assert.assertFalse(job.getStatusMsg().isPresent());
        Assert.assertFalse(job.getCreated().isPresent());
        Assert.assertFalse(job.getDescription().isPresent());
        Assert.assertFalse(job.getId().isPresent());
        Assert.assertThat(job.getTags(), Matchers.empty());
        Assert.assertFalse(job.getUpdated().isPresent());
        Assert.assertThat(job.getRuntime(), Matchers.is(Duration.ZERO));
        Assert.assertThat(job.getGrouping(), Matchers.is(Optional.empty()));
        Assert.assertThat(job.getGroupingInstance(), Matchers.is(Optional.empty()));
    }

    /**
     * Test to make sure can build a valid Job with optional parameters.
     */
    @Test
    @SuppressWarnings("deprecation")
    public void canBuildJobWithOptionalsDeprecated() {
        final Job.Builder builder = new Job.Builder(NAME, USER, VERSION);

        builder.withCommandArgs(COMMAND_ARGS_EXPECTED);

        final String archiveLocation = UUID.randomUUID().toString();
        builder.withArchiveLocation(archiveLocation);

        final String clusterName = UUID.randomUUID().toString();
        builder.withClusterName(clusterName);

        final String commandName = UUID.randomUUID().toString();
        builder.withCommandName(commandName);

        final Instant finished = Instant.now();
        builder.withFinished(finished);

        final Instant started = Instant.now();
        builder.withStarted(started);

        builder.withStatus(JobStatus.SUCCEEDED);

        final String statusMsg = UUID.randomUUID().toString();
        builder.withStatusMsg(statusMsg);

        final Instant created = Instant.now();
        builder.withCreated(created);

        final String description = UUID.randomUUID().toString();
        builder.withDescription(description);

        final String id = UUID.randomUUID().toString();
        builder.withId(id);

        final Set<String> tags = Sets.newHashSet(
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString()
        );
        builder.withTags(tags);

        final Instant updated = Instant.now();
        builder.withUpdated(updated);

        final String grouping = UUID.randomUUID().toString();
        builder.withGrouping(grouping);

        final String groupingInstance = UUID.randomUUID().toString();
        builder.withGroupingInstance(groupingInstance);

        final Job job = builder.build();
        Assert.assertThat(job.getName(), Matchers.is(NAME));
        Assert.assertThat(job.getUser(), Matchers.is(USER));
        Assert.assertThat(job.getVersion(), Matchers.is(VERSION));
        Assert.assertThat(
            job.getCommandArgsString().orElseThrow(IllegalArgumentException::new),
            Matchers.is(COMMAND_ARGS_EXPECTED)
        );
        Assert.assertThat(job.getCommandArgs(), Matchers.is(COMMAND_ARGS));
        Assert.assertThat(
            job.getArchiveLocation().orElseThrow(IllegalArgumentException::new), Matchers.is(archiveLocation)
        );
        Assert.assertThat(job.getClusterName().orElseThrow(IllegalArgumentException::new), Matchers.is(clusterName));
        Assert.assertThat(job.getCommandName().orElseThrow(IllegalArgumentException::new), Matchers.is(commandName));
        Assert.assertThat(job.getFinished().orElseThrow(IllegalArgumentException::new), Matchers.is(finished));
        Assert.assertThat(job.getStarted().orElseThrow(IllegalArgumentException::new), Matchers.is(started));
        Assert.assertThat(job.getStatus(), Matchers.is(JobStatus.SUCCEEDED));
        Assert.assertThat(job.getStatusMsg().orElseThrow(IllegalArgumentException::new), Matchers.is(statusMsg));
        Assert.assertThat(job.getCreated().orElseThrow(IllegalArgumentException::new), Matchers.is(created));
        Assert.assertThat(job.getDescription().orElseThrow(IllegalArgumentException::new), Matchers.is(description));
        Assert.assertThat(job.getId().orElseThrow(IllegalArgumentException::new), Matchers.is(id));
        Assert.assertThat(job.getTags(), Matchers.is(tags));
        Assert.assertThat(job.getUpdated().orElseThrow(IllegalArgumentException::new), Matchers.is(updated));
        Assert.assertThat(
            job.getRuntime(),
            Matchers.is(Duration.ofMillis(finished.toEpochMilli() - started.toEpochMilli()))
        );
        Assert.assertThat(job.getGrouping().orElseThrow(IllegalArgumentException::new), Matchers.is(grouping));
        Assert.assertThat(
            job.getGroupingInstance().orElseThrow(IllegalArgumentException::new),
            Matchers.is(groupingInstance)
        );
    }

    /**
     * Test to make sure can build a valid Job with optional parameters.
     */
    @Test
    public void canBuildJobWithOptionals() {
        final Job.Builder builder = new Job.Builder(NAME, USER, VERSION);

        builder.withCommandArgs(COMMAND_ARGS);

        final String archiveLocation = UUID.randomUUID().toString();
        builder.withArchiveLocation(archiveLocation);

        final String clusterName = UUID.randomUUID().toString();
        builder.withClusterName(clusterName);

        final String commandName = UUID.randomUUID().toString();
        builder.withCommandName(commandName);

        final Instant finished = Instant.now();
        builder.withFinished(finished);

        final Instant started = Instant.now();
        builder.withStarted(started);

        builder.withStatus(JobStatus.SUCCEEDED);

        final String statusMsg = UUID.randomUUID().toString();
        builder.withStatusMsg(statusMsg);

        final Instant created = Instant.now();
        builder.withCreated(created);

        final String description = UUID.randomUUID().toString();
        builder.withDescription(description);

        final String id = UUID.randomUUID().toString();
        builder.withId(id);

        final Set<String> tags = Sets.newHashSet(
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString()
        );
        builder.withTags(tags);

        final Instant updated = Instant.now();
        builder.withUpdated(updated);

        final String grouping = UUID.randomUUID().toString();
        builder.withGrouping(grouping);

        final String groupingInstance = UUID.randomUUID().toString();
        builder.withGroupingInstance(groupingInstance);

        final Job job = builder.build();
        Assert.assertThat(job.getName(), Matchers.is(NAME));
        Assert.assertThat(job.getUser(), Matchers.is(USER));
        Assert.assertThat(job.getVersion(), Matchers.is(VERSION));
        Assert.assertThat(
            job.getCommandArgsString().orElseThrow(IllegalArgumentException::new),
            Matchers.is(COMMAND_ARGS_EXPECTED)
        );
        Assert.assertThat(job.getCommandArgs(), Matchers.is(COMMAND_ARGS));
        Assert.assertThat(
            job.getArchiveLocation().orElseThrow(IllegalArgumentException::new), Matchers.is(archiveLocation)
        );
        Assert.assertThat(job.getClusterName().orElseThrow(IllegalArgumentException::new), Matchers.is(clusterName));
        Assert.assertThat(job.getCommandName().orElseThrow(IllegalArgumentException::new), Matchers.is(commandName));
        Assert.assertThat(job.getFinished().orElseThrow(IllegalArgumentException::new), Matchers.is(finished));
        Assert.assertThat(job.getStarted().orElseThrow(IllegalArgumentException::new), Matchers.is(started));
        Assert.assertThat(job.getStatus(), Matchers.is(JobStatus.SUCCEEDED));
        Assert.assertThat(job.getStatusMsg().orElseThrow(IllegalArgumentException::new), Matchers.is(statusMsg));
        Assert.assertThat(job.getCreated().orElseThrow(IllegalArgumentException::new), Matchers.is(created));
        Assert.assertThat(job.getDescription().orElseThrow(IllegalArgumentException::new), Matchers.is(description));
        Assert.assertThat(job.getId().orElseThrow(IllegalArgumentException::new), Matchers.is(id));
        Assert.assertThat(job.getTags(), Matchers.is(tags));
        Assert.assertThat(job.getUpdated().orElseThrow(IllegalArgumentException::new), Matchers.is(updated));
        Assert.assertThat(
            job.getRuntime(),
            Matchers.is(Duration.ofMillis(finished.toEpochMilli() - started.toEpochMilli()))
        );
        Assert.assertThat(job.getGrouping().orElseThrow(IllegalArgumentException::new), Matchers.is(grouping));
        Assert.assertThat(
            job.getGroupingInstance().orElseThrow(IllegalArgumentException::new),
            Matchers.is(groupingInstance)
        );
    }

    /**
     * Test to make sure a Job can be successfully built when nulls are inputted.
     */
    @Test
    public void canBuildJobWithNulls() {
        final Job.Builder builder = new Job.Builder(NAME, USER, VERSION);
        builder.withCommandArgs((List<String>) null);
        builder.withArchiveLocation(null);
        builder.withClusterName(null);
        builder.withCommandName(null);
        builder.withFinished(null);
        builder.withStarted(null);
        builder.withStatus(JobStatus.INIT);
        builder.withStatusMsg(null);
        builder.withCreated(null);
        builder.withDescription(null);
        builder.withId(null);
        builder.withTags(null);
        builder.withUpdated(null);

        final Job job = builder.build();
        Assert.assertThat(job.getName(), Matchers.is(NAME));
        Assert.assertThat(job.getUser(), Matchers.is(USER));
        Assert.assertThat(job.getVersion(), Matchers.is(VERSION));
        Assert.assertFalse(job.getCommandArgsString().isPresent());
        Assert.assertTrue(job.getCommandArgs().isEmpty());
        Assert.assertFalse(job.getArchiveLocation().isPresent());
        Assert.assertFalse(job.getClusterName().isPresent());
        Assert.assertFalse(job.getCommandName().isPresent());
        Assert.assertFalse(job.getFinished().isPresent());
        Assert.assertFalse(job.getStarted().isPresent());
        Assert.assertThat(job.getStatus(), Matchers.is(JobStatus.INIT));
        Assert.assertFalse(job.getStatusMsg().isPresent());
        Assert.assertFalse(job.getCreated().isPresent());
        Assert.assertFalse(job.getDescription().isPresent());
        Assert.assertFalse(job.getId().isPresent());
        Assert.assertThat(job.getTags(), Matchers.empty());
        Assert.assertFalse(job.getUpdated().isPresent());
        Assert.assertThat(job.getRuntime(), Matchers.is(Duration.ZERO));
    }

    /**
     * Test equals.
     */
    @Test
    public void canFindEquality() {
        final Job.Builder builder = new Job.Builder(NAME, USER, VERSION);
        builder.withCommandArgs((List<String>) null);
        builder.withArchiveLocation(null);
        builder.withClusterName(null);
        builder.withCommandName(null);
        builder.withFinished(null);
        builder.withStarted(null);
        builder.withStatus(JobStatus.INIT);
        builder.withStatusMsg(null);
        builder.withCreated(null);
        builder.withDescription(null);
        builder.withId(UUID.randomUUID().toString());
        builder.withTags(null);
        builder.withUpdated(null);

        final Job job1 = builder.build();
        final Job job2 = builder.build();
        builder.withId(UUID.randomUUID().toString());
        final Job job3 = builder.build();

        Assert.assertEquals(job1, job2);
        Assert.assertEquals(job2, job1);
        Assert.assertNotEquals(job1, job3);
    }

    /**
     * Test hash code.
     */
    @Test
    public void canUseHashCode() {
        final Job.Builder builder = new Job.Builder(NAME, USER, VERSION);
        builder.withCommandArgs((List<String>) null);
        builder.withArchiveLocation(null);
        builder.withClusterName(null);
        builder.withCommandName(null);
        builder.withFinished(null);
        builder.withStarted(null);
        builder.withStatus(JobStatus.INIT);
        builder.withStatusMsg(null);
        builder.withCreated(null);
        builder.withDescription(null);
        builder.withId(UUID.randomUUID().toString());
        builder.withTags(null);
        builder.withUpdated(null);

        final Job job1 = builder.build();
        final Job job2 = builder.build();
        builder.withId(UUID.randomUUID().toString());
        final Job job3 = builder.build();

        Assert.assertEquals(job1.hashCode(), job2.hashCode());
        Assert.assertNotEquals(job1.hashCode(), job3.hashCode());
    }
}
