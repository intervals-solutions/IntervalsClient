package com.intervals.client;

import com.intervals.client.auth.Auth;
import com.intervals.client.auth.NoAuth;
import com.intervals.client.execution.Execution;
import com.intervals.client.execution.HelloWorldExecution;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IntervalsConsumerBuilderTest {

    @Test
    void givenTenantName_whenConstruct_thenTenantNameIsSetAndHasDefaults() {
        // when
        IntervalsConsumerBuilder builder = new IntervalsConsumerBuilder("myTenant");

        // then
        assertThat(builder.getTenantName()).isEqualTo("myTenant");
        assertThat(builder.getHosts()).isEqualTo("kafka.intervals.solutions:9098");
        assertThat(builder.getAuth()).isInstanceOf(NoAuth.class);
        assertThat(builder.getExecution()).isInstanceOf(HelloWorldExecution.class);
        assertThat(builder.getExecutorService()).isNotNull();
        assertThat(builder.getExecutionExceptionHandler()).isNotNull();
    }

    @Test
    void givenBuilder_whenSetAllFields_thenReturnsUpdatesField() {
        // given
        Auth customAuth = username -> java.util.Collections.emptyMap();
        String hosts = "localhost:9092";
        String tenantName = "t01";
        Execution customExecution = notification -> {};
        ExecutorService customExecutor = Executors.newSingleThreadExecutor();
        Consumer<Throwable> customHandler = throwable -> {};

        // when
        IntervalsConsumerBuilder builder = new IntervalsConsumerBuilder(tenantName)
                .hosts(hosts)
                .auth(customAuth)
                .execute(customExecution)
                .executorService(customExecutor)
                .executionExceptionHandler(customHandler);

        // then
        assertThat(builder.getTenantName()).isEqualTo(tenantName);
        assertThat(builder.getHosts()).isEqualTo(hosts);
        assertThat(builder.getAuth()).isSameAs(customAuth);
        assertThat(builder.getExecution()).isSameAs(customExecution);
        assertThat(builder.getExecutorService()).isSameAs(customExecutor);
        assertThat(builder.getExecutionExceptionHandler()).isSameAs(customHandler);
    }

    @Test
    void givenNullTenantName_whenConstruct_thenThrows() {
        // when-then
        assertThatThrownBy(() -> new IntervalsConsumerBuilder(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("tenantName", "should not be null.");
    }

    @Test
    void givenNullHosts_whenSet_thenThrows() {
        // given
        IntervalsConsumerBuilder builder = new IntervalsConsumerBuilder("t01");

        // when-then
        assertThatThrownBy(() -> builder.hosts(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("hosts", "should not be null.");
    }

    @Test
    void givenNullAuth_whenSet_thenThrows() {
        // given
        IntervalsConsumerBuilder builder = new IntervalsConsumerBuilder("t01");

        // when-then
        assertThatThrownBy(() -> builder.auth(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("auth", "should not be null.");
    }

    @Test
    void givenNullExecution_whenSet_thenThrows() {
        // given
        IntervalsConsumerBuilder builder = new IntervalsConsumerBuilder("t01");

        // when-then
        assertThatThrownBy(() -> builder.execute(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("execution", "should not be null.");
    }

    @Test
    void givenNullExecutorService_whenSet_thenThrows() {
        // given
        IntervalsConsumerBuilder builder = new IntervalsConsumerBuilder("t01");

        // when-then
        assertThatThrownBy(() -> builder.executorService(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("executorService", "should not be null.");
    }

    @Test
    void givenNullExecutionExceptionHandler_whenSet_thenThrows() {
        // given
        IntervalsConsumerBuilder builder = new IntervalsConsumerBuilder("t01");

        // when-then
        assertThatThrownBy(() -> builder.executionExceptionHandler(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("executionExceptionHandler", "should not be null.");
    }
}
