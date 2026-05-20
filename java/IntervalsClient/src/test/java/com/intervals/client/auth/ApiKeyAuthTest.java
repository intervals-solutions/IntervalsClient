package com.intervals.client.auth;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.config.SaslConfigs;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ApiKeyAuthTest {

    @Test
    void givenApiKeyAuth_whenGetKafkaProperties_thenAllPropertiesSet() {
        // given
        String username = "alice";
        String password = "myPassword";
        ApiKeyAuth auth = new ApiKeyAuth(password);

        // when
        Map<String, Object> props = auth.getKafkaProperties(username);

        // then
        assertThat(props)
                .containsEntry(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL")
                .containsEntry(SaslConfigs.SASL_MECHANISM, "SCRAM-SHA-512")
                .containsEntry(SaslConfigs.SASL_JAAS_CONFIG, getJassConfig(username, password));
    }

    private static String getJassConfig(String username, String password) {
        return String.format(
                "org.apache.kafka.common.security.scram.ScramLoginModule required " +
                        "username=\"%s\" password=\"%s\";",
                username,
                password
        );
    }
}
