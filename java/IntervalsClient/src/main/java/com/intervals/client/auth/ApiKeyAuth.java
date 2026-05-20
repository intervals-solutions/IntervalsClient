package com.intervals.client.auth;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.config.SaslConfigs;

import java.util.HashMap;
import java.util.Map;

public class ApiKeyAuth implements Auth {
    private final String password;

    public ApiKeyAuth(String apiKey) {
        this.password = apiKey;
    }

    @Override
    public Map<String, Object> getKafkaProperties(String username) {
        Map<String, Object> producerProps = new HashMap<>();
        producerProps.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
        producerProps.put(SaslConfigs.SASL_MECHANISM, "SCRAM-SHA-512");
        String jassConfig = String.format(
                "org.apache.kafka.common.security.scram.ScramLoginModule required " +
                        "username=\"%s\" password=\"%s\";",
                username,
                password
        );
        producerProps.put(SaslConfigs.SASL_JAAS_CONFIG, jassConfig);
        return producerProps;
    }
}
