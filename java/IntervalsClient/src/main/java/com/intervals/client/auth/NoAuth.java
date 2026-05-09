package com.intervals.client.auth;

import java.util.HashMap;
import java.util.Map;

public class NoAuth implements Auth {
    @Override
    public Map<String, Object> getKafkaProperties(String username) {
        return new HashMap<>();
    }
}
