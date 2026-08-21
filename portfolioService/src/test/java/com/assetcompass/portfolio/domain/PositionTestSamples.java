package com.assetcompass.portfolio.domain;

import java.util.UUID;

public class PositionTestSamples {

    public static Position getPositionSample1() {
        return new Position().id(UUID.fromString("23d8dc04-a48b-45d9-a01d-4b728f0ad4aa")).currency("currency1");
    }

    public static Position getPositionSample2() {
        return new Position().id(UUID.fromString("ad79f240-3727-46c3-b89f-2cf6ebd74367")).currency("currency2");
    }

    public static Position getPositionRandomSampleGenerator() {
        return new Position().id(UUID.randomUUID()).currency(UUID.randomUUID().toString());
    }
}
