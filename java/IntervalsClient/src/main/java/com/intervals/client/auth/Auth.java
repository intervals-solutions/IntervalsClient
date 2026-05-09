package com.intervals.client.auth;

import java.util.Map;

public interface Auth {
    Map<String, Object> getKafkaProperties(String username);
}
