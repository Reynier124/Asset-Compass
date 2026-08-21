package com.assetcompass.gateway.domain;

import static com.assetcompass.gateway.domain.AssetTestSamples.*;
import static com.assetcompass.gateway.domain.BrokerAccountTestSamples.*;
import static com.assetcompass.gateway.domain.IncomeEventTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.assetcompass.gateway.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class IncomeEventTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(IncomeEvent.class);
        IncomeEvent incomeEvent1 = getIncomeEventSample1();
        IncomeEvent incomeEvent2 = new IncomeEvent();
        assertThat(incomeEvent1).isNotEqualTo(incomeEvent2);

        incomeEvent2.setId(incomeEvent1.getId());
        assertThat(incomeEvent1).isEqualTo(incomeEvent2);

        incomeEvent2 = getIncomeEventSample2();
        assertThat(incomeEvent1).isNotEqualTo(incomeEvent2);
    }

    @Test
    void accountTest() {
        IncomeEvent incomeEvent = getIncomeEventRandomSampleGenerator();
        BrokerAccount brokerAccountBack = getBrokerAccountRandomSampleGenerator();

        incomeEvent.setAccount(brokerAccountBack);
        assertThat(incomeEvent.getAccount()).isEqualTo(brokerAccountBack);

        incomeEvent.account(null);
        assertThat(incomeEvent.getAccount()).isNull();
    }

    @Test
    void assetTest() {
        IncomeEvent incomeEvent = getIncomeEventRandomSampleGenerator();
        Asset assetBack = getAssetRandomSampleGenerator();

        incomeEvent.setAsset(assetBack);
        assertThat(incomeEvent.getAsset()).isEqualTo(assetBack);

        incomeEvent.asset(null);
        assertThat(incomeEvent.getAsset()).isNull();
    }
}
