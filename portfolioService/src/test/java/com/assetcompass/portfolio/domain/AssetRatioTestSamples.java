package com.assetcompass.portfolio.domain;

import java.util.UUID;

public class AssetRatioTestSamples {

    public static AssetRatio getAssetRatioSample1() {
        return new AssetRatio().id(UUID.fromString("23d8dc04-a48b-45d9-a01d-4b728f0ad4aa")).ratio("ratio1");
    }

    public static AssetRatio getAssetRatioSample2() {
        return new AssetRatio().id(UUID.fromString("ad79f240-3727-46c3-b89f-2cf6ebd74367")).ratio("ratio2");
    }

    public static AssetRatio getAssetRatioRandomSampleGenerator() {
        return new AssetRatio().id(UUID.randomUUID()).ratio(UUID.randomUUID().toString());
    }
}
