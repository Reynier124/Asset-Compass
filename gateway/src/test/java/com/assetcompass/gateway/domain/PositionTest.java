package com.assetcompass.gateway.domain;

import static com.assetcompass.gateway.domain.AssetTestSamples.*;
import static com.assetcompass.gateway.domain.BrokerAccountTestSamples.*;
import static com.assetcompass.gateway.domain.PositionTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.assetcompass.gateway.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class PositionTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Position.class);
        Position position1 = getPositionSample1();
        Position position2 = new Position();
        assertThat(position1).isNotEqualTo(position2);

        position2.setId(position1.getId());
        assertThat(position1).isEqualTo(position2);

        position2 = getPositionSample2();
        assertThat(position1).isNotEqualTo(position2);
    }

    @Test
    void accountTest() {
        Position position = getPositionRandomSampleGenerator();
        BrokerAccount brokerAccountBack = getBrokerAccountRandomSampleGenerator();

        position.setAccount(brokerAccountBack);
        assertThat(position.getAccount()).isEqualTo(brokerAccountBack);

        position.account(null);
        assertThat(position.getAccount()).isNull();
    }

    @Test
    void assetTest() {
        Position position = getPositionRandomSampleGenerator();
        Asset assetBack = getAssetRandomSampleGenerator();

        position.setAsset(assetBack);
        assertThat(position.getAsset()).isEqualTo(assetBack);

        position.asset(null);
        assertThat(position.getAsset()).isNull();
    }
}
