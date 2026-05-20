package com.intervals.client;

import com.intervals.client.auth.ApiKeyAuth;
import com.intervals.client.execution.Execution;
import com.intervals.client.notification.Notification;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.Mockito.verify;

public class IntervalsClientTest {
    private static final String intervalsHosts = "kafka.intervals.solutions:19098,kafka.intervals.solutions:29098";
    private static String tenantName;
    private static String producerPassword;
    private static String consumerPassword;

    @BeforeAll
    static void setUp() {
        tenantName = System.getenv("TENANT_NAME");
        producerPassword = System.getenv("PRODUCER_PASSWORD");
        consumerPassword = System.getenv("CONSUMER_PASSWORD");
    }

    @Test
    void givenSingleExecution_whenProduceAndConsume_thenExecutionInvoked() {
        // given
        Duration delay = Duration.ofSeconds(10);
        Instant schedule = Instant.now().plus(delay);
        Notification notification = new Notification(
                Collections.singletonMap("test-id", UUID.randomUUID().toString()),
                "test content"
        );
        Execution execution = Mockito.mock(Execution.class);

        // when
        try (IntervalsProducer client = createProducer()) {
            client.execute(schedule, notification);
        }

        try (IntervalsConsumer consumer = createConsumer(execution)) {
            consumer.start();

            // then
            Awaitility.await()
                    .atLeast(delay.minusSeconds(1))
                    .atMost(delay.multipliedBy(2))
                    .untilAsserted(() -> verify(execution).execute(Mockito.argThat(received ->
                            received.getContent().equals(notification.getContent()) &&
                                    received.getHeaders().entrySet().containsAll(notification.getHeaders().entrySet())
                    )));
        }
    }

    private static IntervalsProducer createProducer() {
        IntervalsProducerBuilder producerBuilder = new IntervalsProducerBuilder(tenantName)
                .auth(new ApiKeyAuth(producerPassword))
                .hosts(intervalsHosts);
        return producerBuilder.build();
    }

    private static IntervalsConsumer createConsumer(Execution execution) {
        IntervalsConsumerBuilder consumerBuilder = new IntervalsConsumerBuilder(tenantName)
                .auth(new ApiKeyAuth(consumerPassword))
                .hosts(intervalsHosts)
                .execute(execution);
        return consumerBuilder.build();
    }

}
