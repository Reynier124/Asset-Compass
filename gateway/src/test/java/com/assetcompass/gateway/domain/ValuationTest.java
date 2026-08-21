package com.assetcompass.gateway.domain;

import static com.assetcompass.gateway.domain.BrokerAccountTestSamples.*;
import static com.assetcompass.gateway.domain.ValuationTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.assetcompass.gateway.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ValuationTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Valuation.class);
        Valuation valuation1 = getValuationSample1();
        Valuation valuation2 = new Valuation();
        assertThat(valuation1).isNotEqualTo(valuation2);

        valuation2.setId(valuation1.getId());
        assertThat(valuation1).isEqualTo(valuation2);

        valuation2 = getValuationSample2();
        assertThat(valuation1).isNotEqualTo(valuation2);
    }

    @Test
    void accountTest() {
        Valuation valuation = getValuationRandomSampleGenerator();
        BrokerAccount brokerAccountBack = getBrokerAccountRandomSampleGenerator();

        valuation.setAccount(brokerAccountBack);
        assertThat(valuation.getAccount()).isEqualTo(brokerAccountBack);

        valuation.account(null);
        assertThat(valuation.getAccount()).isNull();
    }
}
