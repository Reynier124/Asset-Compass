package com.assetcompass.portfolio.domain;

import java.util.UUID;

public class BrokerAccountTestSamples {

    public static BrokerAccount getBrokerAccountSample1() {
        return new BrokerAccount()
            .id(UUID.fromString("23d8dc04-a48b-45d9-a01d-4b728f0ad4aa"))
            .externalAccountId("externalAccountId1")
            .displayName("displayName1");
    }

    public static BrokerAccount getBrokerAccountSample2() {
        return new BrokerAccount()
            .id(UUID.fromString("ad79f240-3727-46c3-b89f-2cf6ebd74367"))
            .externalAccountId("externalAccountId2")
            .displayName("displayName2");
    }

    public static BrokerAccount getBrokerAccountRandomSampleGenerator() {
        return new BrokerAccount()
            .id(UUID.randomUUID())
            .externalAccountId(UUID.randomUUID().toString())
            .displayName(UUID.randomUUID().toString());
    }
}
