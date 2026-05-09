package com.intervals.client.schedule;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class SingleExecutionTest {

    @Test
    void givenInstant_whenGetScheduleString_thenReturnsInstantString() {
        // given
        Instant instant = Instant.parse("2024-01-15T10:30:00Z");
        SingleExecution singleExecution = new SingleExecution(instant);

        // when
        String result = singleExecution.getScheduleString();

        // then
        assertThat(result).isEqualTo("2024-01-15T10:30:00Z");
    }
}
