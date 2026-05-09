package com.intervals.client;

import com.intervals.client.auth.Auth;
import com.intervals.client.auth.NoAuth;
import com.intervals.client.execution.Execution;
import com.intervals.client.execution.HelloWorldExecution;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class IntervalsConsumerBuilder {
    private final String tenantName;
    private String hosts = "kafka.intervals.solutions:9098";
    private Auth auth = new NoAuth();
    private Execution execution = new HelloWorldExecution();
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Consumer<Throwable> executionExceptionHandler = getDefaultExecutionExceptionHandler();


    public IntervalsConsumerBuilder(String tenantName) {
        requireNonNull(tenantName, "tenantName");
        this.tenantName = tenantName;
    }

    public IntervalsConsumer build() {
        return new IntervalsConsumer(this);
    }

    public IntervalsConsumerBuilder hosts(String hosts) {
        requireNonNull(hosts, "hosts");
        this.hosts = hosts;
        return this;
    }

    public IntervalsConsumerBuilder auth(Auth auth) {
        requireNonNull(auth, "auth");
        this.auth = auth;
        return this;
    }


    public IntervalsConsumerBuilder execute(Execution execution) {
        requireNonNull(execution, "execution");
        this.execution = execution;
        return this;
    }

    public IntervalsConsumerBuilder executorService(ExecutorService executorService) {
        requireNonNull(executorService, "executorService");
        this.executorService = executorService;
        return this;
    }

    public IntervalsConsumerBuilder executionExceptionHandler(Consumer<Throwable> executionExceptionHandler) {
        requireNonNull(executionExceptionHandler, "executionExceptionHandler");
        this.executionExceptionHandler = executionExceptionHandler;
        return this;
    }

    public String getTenantName() {
        return tenantName;
    }

    public String getHosts() {
        return hosts;
    }

    public Auth getAuth() {
        return auth;
    }

    public Execution getExecution() {
        return execution;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public Consumer<Throwable> getExecutionExceptionHandler() {
        return executionExceptionHandler;
    }

    private static Consumer<Throwable> getDefaultExecutionExceptionHandler() {
        return throwable -> LoggerFactory.getLogger(IntervalsProducer.class)
                .error("Failed to execute notification for record", throwable);
    }

    private static <T> void requireNonNull(T object, String name) {
        Objects.requireNonNull(
                object,
                String.format("Value of '%s' parameter should not be null.", name)
        );
    }

}
