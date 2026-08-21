package com.assetcompass.gateway.domain;

import static com.assetcompass.gateway.domain.AssetRatioTestSamples.*;
import static com.assetcompass.gateway.domain.AssetTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.assetcompass.gateway.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class AssetRatioTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(AssetRatio.class);
        AssetRatio assetRatio1 = getAssetRatioSample1();
        AssetRatio assetRatio2 = new AssetRatio();
        assertThat(assetRatio1).isNotEqualTo(assetRatio2);

        assetRatio2.setId(assetRatio1.getId());
        assertThat(assetRatio1).isEqualTo(assetRatio2);

        assetRatio2 = getAssetRatioSample2();
        assertThat(assetRatio1).isNotEqualTo(assetRatio2);
    }

    @Test
    void assetTest() {
        AssetRatio assetRatio = getAssetRatioRandomSampleGenerator();
        Asset assetBack = getAssetRandomSampleGenerator();

        assetRatio.setAsset(assetBack);
        assertThat(assetRatio.getAsset()).isEqualTo(assetBack);

        assetRatio.asset(null);
        assertThat(assetRatio.getAsset()).isNull();
    }
}
