package com.intervals.client;

import com.intervals.client.auth.NoAuth;
import com.intervals.client.notification.Notification;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
class IntervalsProducerTest {
    private static final Duration POLL_TIMEOUT = Duration.ofSeconds(20);

    @Container
    static ConfluentKafkaContainer kafka = new ConfluentKafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.6.0")
    );

    @Test
    @Timeout(30)
    void givenSingleExecutionSchedule_whenSendMessage_thenMessageDelivered() {
        // given
        String tenant = UUID.randomUUID().toString();
        String testContent = "test-content";
        Map<String, String> testHeaders = Collections.singletonMap("test-id", UUID.randomUUID().toString());
        Notification notification = new Notification(testHeaders, testContent);
        Instant schedule = Instant.now().plus(Duration.ofSeconds(10));

        // when
        try (IntervalsProducer producer = new IntervalsProducerBuilder(tenant)
                .hosts(kafka.getBootstrapServers())
                .auth(new NoAuth())
                .build()) {
            producer.execute(schedule, notification);
        }

        // then
        ConsumerRecord<String, String> record = consumeFirstRecord(tenant);
        assertThat(record.value()).isEqualTo(testContent);

        Map<String, String> recordHeaders = toHeaderMap(record);
        assertThat(recordHeaders).containsAllEntriesOf(testHeaders);
        assertThat(recordHeaders).containsEntry("intervals-schedule", schedule.toString());
    }

    @Test
    @Timeout(20)
    void givenInvalidHosts_whenSendMessage_thenThrows() {
        // given
        String tenant = UUID.randomUUID().toString();
        Notification notification = new Notification(
                Collections.singletonMap("h", "v"), "content"
        );

        // when-then
        try (IntervalsProducer producer = new IntervalsProducerBuilder(tenant)
                .hosts("localhost:1")
                .auth(new NoAuth())
                .build()) {
            assertThatThrownBy(() -> producer.execute(Instant.now(), notification))
                    .isInstanceOf(FailedToSendException.class);
        }
    }

    @Test
    @Timeout(5)
    void givenNullNotification_whenExecute_thenThrows() {
        // given
        String tenant = UUID.randomUUID().toString();

        try (IntervalsProducer producer = new IntervalsProducerBuilder(tenant)
                .hosts(kafka.getBootstrapServers())
                .auth(new NoAuth())
                .build()) {

            // when-then
            assertThatThrownBy(() -> producer.execute(Instant.now(), null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Test
    @Timeout(5)
    void givenNullSchedule_whenExecute_thenThrows() {
        // given
        String tenant = UUID.randomUUID().toString();
        Notification notification = new Notification(
                Collections.singletonMap("h", "v"), "content"
        );

        try (IntervalsProducer producer = new IntervalsProducerBuilder(tenant)
                .hosts(kafka.getBootstrapServers())
                .auth(new NoAuth())
                .build()) {

            // when-then
            assertThatThrownBy(() -> producer.execute((Instant) null, notification))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    private static ConsumerRecord<String, String> consumeFirstRecord(String tenant) {
        String topic = String.format("assignments.%s", tenant);

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "component-test-consumer");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList(topic));

            long deadline = System.nanoTime() + POLL_TIMEOUT.toNanos();
            while (System.nanoTime() < deadline) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(200));
                for (ConsumerRecord<String, String> record : records) {
                    return record;
                }
            }
        }
        throw new AssertionError(String.format("No message consumed within %s", POLL_TIMEOUT));
    }

    private static Map<String, String> toHeaderMap(ConsumerRecord<String, String> record) {
        return StreamSupport.stream(record.headers().spliterator(), false)
                .collect(Collectors.toMap(Header::key, h -> new String(h.value())));
    }
}
