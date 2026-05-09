package com.intervals.client.auth;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NoAuthTest {

    @Test
    void givenNoAuth_whenGetKafkaProperties_thenReturnsEmptyMap() {
        // given
        NoAuth noAuth = new NoAuth();

        // when
        java.util.Map<String, Object> properties = noAuth.getKafkaProperties("testUser");

        // then
        assertThat(properties).isEmpty();
    }
}
