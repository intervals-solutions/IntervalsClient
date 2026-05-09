package com.intervals.client;

import com.intervals.client.auth.Auth;
import com.intervals.client.notification.Notification;
import com.intervals.client.schedule.Schedule;
import com.intervals.client.schedule.SingleExecution;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.serialization.StringSerializer;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntervalsProducer implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(IntervalsProducer.class);
    private final KafkaProducer<String, String> producer;
    private final String tenantName;

    IntervalsProducer(IntervalsProducerBuilder builder) {
        this.tenantName = builder.getTenantName();
        this.producer = createProducer(builder.getAuth(), builder.getHosts());
    }

    private KafkaProducer<String, String> createProducer(Auth auth, String bootstrapServers) {
        String username = String.format("assignments-producer-%s", tenantName);

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.LINGER_MS_CONFIG, (int) Duration.ofMillis(50).toMillis());
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, (int) Duration.ofSeconds(8).toMillis());
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, (int) Duration.ofSeconds(10).toMillis());
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, (int) Duration.ofSeconds(10).toMillis());
        props.putAll(auth.getKafkaProperties(username));

        return new KafkaProducer<>(props);
    }

    public void execute(Instant instant, Notification notification) {
        execute(new SingleExecution(instant), notification);
    }

    public void execute(Schedule schedule, Notification notification) {
        Objects.requireNonNull(schedule, "Schedule should not be null.");
        Objects.requireNonNull(notification, "Notification should not be null.");
        String topic = String.format("assignments.%s", tenantName);
        RecordHeader scheduleHeader = new RecordHeader(
                "intervals-schedule",
                schedule.getScheduleString().getBytes(StandardCharsets.UTF_8)
        );
        List<Header> headers = notification.getHeaders().entrySet().stream()
                .map(e -> new RecordHeader(e.getKey(), e.getValue().getBytes(StandardCharsets.UTF_8)))
                .collect(Collectors.toList());
        headers.add(scheduleHeader);

        ProducerRecord<String, String> record = new ProducerRecord<>(
                topic,
                null,
                null,
                null,
                notification.getContent(),
                headers
        );
        try {
            producer.send(record).get(Duration.ofSeconds(10).toMillis(), TimeUnit.MILLISECONDS);
            log.info("Producer sent notification. Notification headers: {}", notification.getHeaders());
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new FailedToSendException("Failed to send notification to Intervals.", e);
        }
    }

    @Override
    public void close() {
        producer.close();
    }
}
