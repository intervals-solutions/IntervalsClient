package com.intervals.client.execution;

import com.intervals.client.notification.Notification;

@FunctionalInterface
public interface Execution {
    void execute(Notification notification);
}
