package com.intervals.client.schedule;

import java.time.Instant;

public class SingleExecution implements Schedule {
    private final Instant instant;

    public SingleExecution(Instant instant) {
        this.instant = instant;
    }

    @Override
    public String getScheduleString() {
        return instant.toString();
    }
}
