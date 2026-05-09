package com.intervals.client.execution;

import com.intervals.client.notification.Notification;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

class HelloWorldExecutionTest {

    private final ByteArrayOutputStream output = new ByteArrayOutputStream();
    private final PrintStream original = System.out;

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(output));
    }

    @AfterEach
    void tearDown() {
        System.setOut(original);
    }

    @Test
    void givenHelloWorldExecution_whenExecute_thenPrintsExpectedMessage() {
        // given
        HelloWorldExecution execution = new HelloWorldExecution();
        Notification notification = new Notification(new HashMap<>(), "test");

        // when
        execution.execute(notification);

        // then
        assertThat(output.toString()).contains("Hello world from Intervals!");
    }
}
