package com.assetcompass.gateway.domain;

import java.util.UUID;

public class BrokerTestSamples {

    public static Broker getBrokerSample1() {
        return new Broker().id(UUID.fromString("23d8dc04-a48b-45d9-a01d-4b728f0ad4aa")).name("name1");
    }

    public static Broker getBrokerSample2() {
        return new Broker().id(UUID.fromString("ad79f240-3727-46c3-b89f-2cf6ebd74367")).name("name2");
    }

    public static Broker getBrokerRandomSampleGenerator() {
        return new Broker().id(UUID.randomUUID()).name(UUID.randomUUID().toString());
    }
}
