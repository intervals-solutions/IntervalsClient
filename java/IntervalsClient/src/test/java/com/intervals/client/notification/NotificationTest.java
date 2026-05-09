package com.intervals.client.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificationTest {

    @Test
    void givenFields_whenConstruct_thenFieldsSet() {
        // given
        Map<String, String> headers = mapOf(
                "type", "alert",
                "severity", "high"
        );

        // when
        Notification notification = new Notification(headers, "test content");

        // then
        assertThat(notification.getHeaders()).isEqualTo(headers);
        assertThat(notification.getHeaders()).isNotSameAs(headers);
        assertThat(notification.getContent()).isEqualTo("test content");
    }

    @Test
    void givenEqualNotifications_whenEquals_thenTrue() {
        // given
        Notification a = new Notification(mapOf("key", "value"), "content");
        Notification b = new Notification(mapOf("key", "value"), "content");

        // then
        assertThat(a).isEqualTo(b);
        assertThat(b).isEqualTo(a);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("notEqualScenarios")
    void givenDifferent_whenEquals_thenFalse(String scenario, Notification notification, Object other) {
        assertThat(notification).isNotEqualTo(other);
    }

    @Test
    void givenNullHeaders_whenConstruct_thenThrows() {
        // given / when / then
        assertThatThrownBy(() -> new Notification(null, "content"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Headers should not be null");
    }

    @Test
    void givenNotification_whenHashCodeTwice_thenConsistent() {
        // given
        Notification notification = new Notification(mapOf("key", "value"), "content");

        // when
        int h1 = notification.hashCode();
        int h2 = notification.hashCode();

        // then
        assertThat(h1).isEqualTo(h2);
    }

    @Test
    void givenEqualNotifications_whenHashCode_thenEqual() {
        // given
        Notification a = new Notification(mapOf("key", "value"), "content");
        Notification b = new Notification(mapOf("key", "value"), "content");

        // when
        int aHashCode = a.hashCode();
        int bHashCode = b.hashCode();

        // then
        assertThat(aHashCode).isEqualTo(bHashCode);
    }

    @Test
    void givenNotificationWithHeaders_whenToString_thenContainsContentAndHeaders() {
        // given
        Notification notification = new Notification(mapOf("type", "alert"), "hello");

        // when
        String actual = notification.toString();

        // then
        assertThat(actual)
                .contains("hello")
                .contains("type:alert")
                .startsWith("Notification{")
                .endsWith("}");
    }

    @Test
    void givenNotificationWithMultipleHeaders_whenToString_thenContainsAllHeaders() {
        // given
        Map<String, String> headers = mapOf(
                "a", "1",
                "b", "2"
        );
        Notification notification = new Notification(headers, "test");

        // when
        String str = notification.toString();

        // then
        assertThat(str)
                .contains("a:1")
                .contains("b:2");
    }

    @Test
    void givenNotificationWithEmptyHeaders_whenToString_thenReturnsExpectedFormat() {
        // given
        Notification notification = new Notification(new HashMap<>(), "test");

        // when
        String str = notification.toString();

        // then
        assertThat(str).isEqualTo("Notification{content=test, headers={}}");
    }

    private static Stream<Arguments> notEqualScenarios() {
        Notification base = new Notification(mapOf("key", "value"), "content");
        return Stream.of(
                Arguments.of(
                        "different headers",
                        new Notification(mapOf("key", "other"), "content"),
                        base
                ),
                Arguments.of(
                        "different content",
                        new Notification(mapOf("key", "value"), "other"),
                        base
                ),
                Arguments.of(
                        "null",
                        base,
                        null
                ),
                Arguments.of(
                        "different type",
                        base,
                        "not a notification"
                )
        );
    }

    private static Map<String, String> mapOf(String... entries) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < entries.length; i += 2) {
            map.put(entries[i], entries[i + 1]);
        }
        return map;
    }
}
