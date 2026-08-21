package com.assetcompass.gateway.domain;

import java.util.UUID;

public class OperationTestSamples {

    public static Operation getOperationSample1() {
        return new Operation().id(UUID.fromString("23d8dc04-a48b-45d9-a01d-4b728f0ad4aa")).currency("currency1");
    }

    public static Operation getOperationSample2() {
        return new Operation().id(UUID.fromString("ad79f240-3727-46c3-b89f-2cf6ebd74367")).currency("currency2");
    }

    public static Operation getOperationRandomSampleGenerator() {
        return new Operation().id(UUID.randomUUID()).currency(UUID.randomUUID().toString());
    }
}
