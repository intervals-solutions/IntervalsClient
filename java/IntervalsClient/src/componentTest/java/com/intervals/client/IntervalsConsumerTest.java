package com.intervals.client;

import com.intervals.client.auth.NoAuth;
import com.intervals.client.execution.Execution;
import com.intervals.client.notification.Notification;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import org.testcontainers.utility.DockerImageName;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class IntervalsConsumerTest {

    @Container
    static ConfluentKafkaContainer kafka = new ConfluentKafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.6.0")
    );

    @Test
    @Timeout(30)
    void givenMessageInTopic_whenConsumerStarts_thenExecutionReceivesNotification() {
        // given
        String tenant = UUID.randomUUID().toString();
        String testContent = "consumer-test-content";
        Map<String, String> testHeaders = Collections.singletonMap("source", "consumer-test");

        sendMessage(tenant, testContent, testHeaders);

        // when
        AtomicReference<Notification> received = new AtomicReference<>();
        Execution execution = received::set;

        try (IntervalsConsumer consumer = new IntervalsConsumerBuilder(tenant)
                .hosts(kafka.getBootstrapServers())
                .auth(new NoAuth())
                .execute(execution)
                .build()) {
            consumer.start();

            // then
            Awaitility.await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
                assertThat(received.get()).isNotNull();
                assertThat(received.get().getContent()).isEqualTo(testContent);
                assertThat(received.get().getHeaders()).containsAllEntriesOf(testHeaders);
            });
        }
    }

    private static void sendMessage(String tenant, String content, Map<String, String> headers) {
        String topic = String.format("executions.%s", tenant);

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        ProducerRecord<String, String> record = new ProducerRecord<>(topic, content);
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            record.headers().add(new RecordHeader(
                    entry.getKey(), entry.getValue().getBytes(StandardCharsets.UTF_8)
            ));
        }

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            producer.send(record);
            producer.flush();
        }
    }
}
