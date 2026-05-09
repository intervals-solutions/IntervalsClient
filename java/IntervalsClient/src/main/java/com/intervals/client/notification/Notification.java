package com.intervals.client.notification;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class Notification {
    private final Map<String, String> headers;
    private final String content;

    public Notification(Map<String, String> headers, String content) {
        if (headers == null) {
            throw new IllegalArgumentException("Headers should not be null");
        }
        this.headers = new HashMap<>(headers);
        this.content = content;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return String.format(
                "Notification{content=%s, headers=%s}",
                content,
                headers.entrySet().stream()
                        .map(e -> String.format("%s:%s", e.getKey(), e.getValue()))
                        .collect(Collectors.joining(",", "{", "}"))
        );
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Notification that = (Notification) o;
        return Objects.equals(headers, that.headers) && Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(headers, content);
    }
}
