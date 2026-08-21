package com.assetcompass.portfolio.domain;

import java.util.UUID;

public class IncomeEventTestSamples {

    public static IncomeEvent getIncomeEventSample1() {
        return new IncomeEvent().id(UUID.fromString("23d8dc04-a48b-45d9-a01d-4b728f0ad4aa")).currency("currency1");
    }

    public static IncomeEvent getIncomeEventSample2() {
        return new IncomeEvent().id(UUID.fromString("ad79f240-3727-46c3-b89f-2cf6ebd74367")).currency("currency2");
    }

    public static IncomeEvent getIncomeEventRandomSampleGenerator() {
        return new IncomeEvent().id(UUID.randomUUID()).currency(UUID.randomUUID().toString());
    }
}
