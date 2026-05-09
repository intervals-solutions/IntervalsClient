package com.intervals.client;

import com.intervals.client.auth.Auth;
import com.intervals.client.auth.NoAuth;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IntervalsProducerBuilderTest {

    @Test
    void givenTenantName_whenConstruct_thenTenantNameIsSetAndHasDefaults() {
        // when
        IntervalsProducerBuilder builder = new IntervalsProducerBuilder("myTenant");

        // then
        assertThat(builder.getTenantName()).isEqualTo("myTenant");
        assertThat(builder.getHosts()).isEqualTo("kafka.intervals.solutions:9098");
        assertThat(builder.getAuth()).isInstanceOf(NoAuth.class);
    }

    @Test
    void givenBuilder_whenSetAllFields_thenReturnsUpdatesField() {
        // given
        Auth customAuth = username -> java.util.Collections.emptyMap();
        String hosts = "localhost:9092";
        String tenantName = "t01";

        // when
        IntervalsProducerBuilder builder = new IntervalsProducerBuilder(tenantName)
                .hosts(hosts)
                .auth(customAuth);

        // then
        assertThat(builder.getTenantName()).isEqualTo(tenantName);
        assertThat(builder.getHosts()).isEqualTo(hosts);
        assertThat(builder.getAuth()).isSameAs(customAuth);
    }

    @Test
    void givenNullTenantName_whenConstruct_thenThrows() {
        // when-then
        assertThatThrownBy(() -> new IntervalsProducerBuilder(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("tenantName", "should not be null.");
    }

    @Test
    void givenNullHosts_whenConstruct_thenThrows() {
        // given
        IntervalsProducerBuilder builder = new IntervalsProducerBuilder("t01");

        // when-then
        assertThatThrownBy(() -> builder.hosts(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("hosts", "should not be null.");
    }

    @Test
    void givenNullAuth_whenConstruct_thenThrows() {
        // given
        IntervalsProducerBuilder builder = new IntervalsProducerBuilder("t01");

        // when-then
        assertThatThrownBy(() -> builder.auth(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("auth", "should not be null.");
    }
}
