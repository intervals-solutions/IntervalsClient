package com.intervals.client.execution;

import com.intervals.client.notification.Notification;

public class HelloWorldExecution implements Execution {
    public void execute(Notification notification) {
        System.out.println("Hello world from Intervals!");
    }
}
