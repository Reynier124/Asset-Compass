package com.assetcompass.portfolio.domain;

import java.util.UUID;

public class AssetTestSamples {

    public static Asset getAssetSample1() {
        return new Asset()
            .id(UUID.fromString("23d8dc04-a48b-45d9-a01d-4b728f0ad4aa"))
            .ticket("ticket1")
            .category("category1")
            .country("country1")
            .description("description1");
    }

    public static Asset getAssetSample2() {
        return new Asset()
            .id(UUID.fromString("ad79f240-3727-46c3-b89f-2cf6ebd74367"))
            .ticket("ticket2")
            .category("category2")
            .country("country2")
            .description("description2");
    }

    public static Asset getAssetRandomSampleGenerator() {
        return new Asset()
            .id(UUID.randomUUID())
            .ticket(UUID.randomUUID().toString())
            .category(UUID.randomUUID().toString())
            .country(UUID.randomUUID().toString())
            .description(UUID.randomUUID().toString());
    }
}
