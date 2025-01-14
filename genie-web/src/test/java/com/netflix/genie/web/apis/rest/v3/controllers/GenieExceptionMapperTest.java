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
package com.netflix.genie.web.apis.rest.v3.controllers;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.netflix.genie.common.exceptions.GenieBadRequestException;
import com.netflix.genie.common.exceptions.GenieConflictException;
import com.netflix.genie.common.exceptions.GenieException;
import com.netflix.genie.common.exceptions.GenieNotFoundException;
import com.netflix.genie.common.exceptions.GeniePreconditionException;
import com.netflix.genie.common.exceptions.GenieServerException;
import com.netflix.genie.common.exceptions.GenieServerUnavailableException;
import com.netflix.genie.common.exceptions.GenieTimeoutException;
import com.netflix.genie.web.util.MetricsConstants;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Tests for the exception mapper.
 *
 * @author tgianos
 * @since 3.0.0
 */
public class GenieExceptionMapperTest {

    private HttpServletResponse response;
    private GenieExceptionMapper mapper;
    private MeterRegistry registry;
    private Counter counter;

    /**
     * Setup for the tests.
     */
    @Before
    public void setup() {
        this.response = Mockito.mock(HttpServletResponse.class);
        this.registry = Mockito.mock(MeterRegistry.class);
        this.counter = Mockito.mock(Counter.class);
        Mockito
            .when(
                this.registry.counter(
                    Mockito.eq(GenieExceptionMapper.CONTROLLER_EXCEPTION_COUNTER_NAME),
                    Mockito.anySet()
                )
            )
            .thenReturn(this.counter);
        this.mapper = new GenieExceptionMapper(this.registry);
    }

    /**
     * Test Genie Exceptions.
     *
     * @throws IOException on error
     */
    @Test
    public void canHandleGenieExceptions() throws IOException {
        final List<GenieException> exceptions = Arrays.asList(
            new GenieBadRequestException("bad"),
            new GenieConflictException("conflict"),
            new GenieNotFoundException("Not Found"),
            new GeniePreconditionException("Precondition"),
            new GenieServerException("server"),
            new GenieServerUnavailableException("Server Unavailable"),
            new GenieTimeoutException("Timeout"),
            new GenieException(568, "Other")
        );

        for (final GenieException exception : exceptions) {
            this.mapper.handleGenieException(this.response, exception);
            Mockito
                .verify(this.registry, Mockito.times(1))
                .counter(
                    GenieExceptionMapper.CONTROLLER_EXCEPTION_COUNTER_NAME,
                    Sets.newHashSet(
                        Tag.of(MetricsConstants.TagKeys.EXCEPTION_CLASS, exception.getClass().getCanonicalName())
                    )
                );
        }

        Mockito
            .verify(this.counter, Mockito.times(exceptions.size()))
            .increment();
    }

    /**
     * Test constraint violation exceptions.
     *
     * @throws IOException on error
     */
    @Test
    public void canHandleConstraintViolationExceptions() throws IOException {
        final ConstraintViolationException exception = new ConstraintViolationException("cve", null);
        this.mapper.handleConstraintViolation(this.response, exception);
        Mockito
            .verify(this.response, Mockito.times(1))
            .sendError(Mockito.eq(HttpStatus.PRECONDITION_FAILED.value()), Mockito.anyString());
        Mockito
            .verify(this.registry, Mockito.times(1))
            .counter(
                GenieExceptionMapper.CONTROLLER_EXCEPTION_COUNTER_NAME,
                Sets.newHashSet(
                    Tag.of(MetricsConstants.TagKeys.EXCEPTION_CLASS, exception.getClass().getCanonicalName())
                )
            );
        Mockito
            .verify(this.counter, Mockito.times(1))
            .increment();
    }

    /**
     * Test method argument not valid exceptions.
     *
     * @throws IOException on error
     */
    @Test
    @SuppressFBWarnings(value = "DM_NEW_FOR_GETCLASS", justification = "It's needed for the test")
    public void canHandleMethodArgumentNotValidExceptions() throws IOException {
        // Method is a final class so can't mock it. Just use the current method.
        final Method method = new Object() {
        }.getClass().getEnclosingMethod();
        final MethodParameter parameter = Mockito.mock(MethodParameter.class);
        Mockito.when(parameter.getMethod()).thenReturn(method);
        final Executable executable = Mockito.mock(Executable.class);
        Mockito.when(parameter.getExecutable()).thenReturn(executable);
        Mockito.when(executable.toGenericString()).thenReturn(UUID.randomUUID().toString());

        final BindingResult bindingResult = Mockito.mock(BindingResult.class);
        Mockito.when(bindingResult.getAllErrors()).thenReturn(Lists.newArrayList());

        final MethodArgumentNotValidException exception = new MethodArgumentNotValidException(
            parameter,
            bindingResult
        );

        this.mapper.handleMethodArgumentNotValidException(this.response, exception);
        Mockito
            .verify(this.response, Mockito.times(1))
            .sendError(Mockito.eq(HttpStatus.PRECONDITION_FAILED.value()), Mockito.anyString());
        Mockito
            .verify(this.registry, Mockito.times(1))
            .counter(
                GenieExceptionMapper.CONTROLLER_EXCEPTION_COUNTER_NAME,
                Sets.newHashSet(
                    Tag.of(MetricsConstants.TagKeys.EXCEPTION_CLASS, exception.getClass().getCanonicalName())
                )
            );
        Mockito
            .verify(this.counter, Mockito.times(1))
            .increment();
    }
}
