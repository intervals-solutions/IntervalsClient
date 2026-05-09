package com.intervals.client;

import com.intervals.client.auth.Auth;
import com.intervals.client.auth.NoAuth;

import java.util.Objects;

public class IntervalsProducerBuilder {
    private final String tenantName;
    private String hosts = "kafka.intervals.solutions:9098";
    private Auth auth = new NoAuth();

    public IntervalsProducerBuilder(String tenantName) {
        Objects.requireNonNull(tenantName, "Value of 'tenantName' parameter should not be null.");
        this.tenantName = tenantName;
    }

    public IntervalsProducer build() {
        return new IntervalsProducer(this);
    }

    public IntervalsProducerBuilder hosts(String hosts) {
        Objects.requireNonNull(hosts, "Value of 'hosts' parameter should not be null.");
        this.hosts = hosts;
        return this;
    }

    public IntervalsProducerBuilder auth(Auth auth) {
        Objects.requireNonNull(auth, "Value of 'auth' parameter should not be null.");
        this.auth = auth;
        return this;
    }


    public String getTenantName() {
        return tenantName;
    }

    public String getHosts() {
        return hosts;
    }

    public Auth getAuth() {
        return auth;
    }

}
