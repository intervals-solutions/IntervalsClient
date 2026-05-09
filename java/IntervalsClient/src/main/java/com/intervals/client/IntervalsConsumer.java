package com.intervals.client;

import com.intervals.client.auth.Auth;
import com.intervals.client.execution.Execution;
import com.intervals.client.notification.Notification;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class IntervalsConsumer implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(IntervalsConsumer.class);
    private final KafkaConsumer<String, String> consumer;
    private final String tenantName;
    private final ExecutorService executor;
    private final Execution execution;
    private final Consumer<Throwable> executionExceptionHandler;
    private volatile boolean running;

    IntervalsConsumer(IntervalsConsumerBuilder builder) {
        this.executor = builder.getExecutorService();
        this.execution = builder.getExecution();
        this.tenantName = builder.getTenantName();
        this.executionExceptionHandler = builder.getExecutionExceptionHandler();
        this.consumer = createConsumer(builder.getAuth(), builder.getHosts());
    }

    public void start() {
        this.running = true;
        executor.submit(this::startConsumer);
    }

    @Override
    public void close() {
        running = false;
    }

    private void startConsumer() {
        try {
            log.info("Starting consumer for tenant: {}.", tenantName);
            while (running) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(50));
                for (ConsumerRecord<String, String> record : records) {
                    log.info(
                            "Received record: topic={}, partition={}, offset={}",
                            record.topic(),
                            record.partition(),
                            record.offset()
                    );
                    try {
                        execution.execute(getNotification(record));
                    } catch (Throwable e) {
                        executionExceptionHandler.accept(e);
                    }
                }
            }
            consumer.close();
        } catch (Throwable e) {
            log.error("Consumer failed for tenant: {}", tenantName, e);
        }
    }


    private KafkaConsumer<String, String> createConsumer(Auth auth, String bootstrapServers) {
        String username = String.format("executions-consumer-%s", tenantName);

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, String.format("executions-consumer-%s", tenantName));
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        props.putAll(auth.getKafkaProperties(username));
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);

        String topicName = String.format("executions.%s", tenantName);
        consumer.subscribe(Collections.singletonList(topicName));

        return consumer;
    }

    private static Notification getNotification(ConsumerRecord<String, String> record) {
        Map<String, String> headers = Arrays.stream(record.headers().toArray())
                .collect(Collectors.toMap(Header::key, h -> new String(h.value())));
        return new Notification(headers, record.value());
    }
}
