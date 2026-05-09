package com.intervals.client.auth;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;

import java.util.HashMap;
import java.util.Map;

public class ApiKeyAuth implements Auth {
    private final String password;
    private final String truststoreLocation;
    private final String truststorePassword;

    public ApiKeyAuth(
            String password,
            String truststoreLocation,
            String truststorePassword
    ) {
        this.password = password;
        this.truststoreLocation = truststoreLocation;
        this.truststorePassword = truststorePassword;
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
        producerProps.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, truststoreLocation);
        producerProps.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, truststorePassword);
        return producerProps;
    }
}
