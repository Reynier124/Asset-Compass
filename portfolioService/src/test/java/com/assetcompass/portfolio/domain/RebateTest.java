package com.assetcompass.portfolio.domain;

import static com.assetcompass.portfolio.domain.BrokerAccountTestSamples.*;
import static com.assetcompass.portfolio.domain.OperationTestSamples.*;
import static com.assetcompass.portfolio.domain.RebateTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.assetcompass.portfolio.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class RebateTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Rebate.class);
        Rebate rebate1 = getRebateSample1();
        Rebate rebate2 = new Rebate();
        assertThat(rebate1).isNotEqualTo(rebate2);

        rebate2.setId(rebate1.getId());
        assertThat(rebate1).isEqualTo(rebate2);

        rebate2 = getRebateSample2();
        assertThat(rebate1).isNotEqualTo(rebate2);
    }

    @Test
    void accountTest() {
        Rebate rebate = getRebateRandomSampleGenerator();
        BrokerAccount brokerAccountBack = getBrokerAccountRandomSampleGenerator();

        rebate.setAccount(brokerAccountBack);
        assertThat(rebate.getAccount()).isEqualTo(brokerAccountBack);

        rebate.account(null);
        assertThat(rebate.getAccount()).isNull();
    }

    @Test
    void operationTest() {
        Rebate rebate = getRebateRandomSampleGenerator();
        Operation operationBack = getOperationRandomSampleGenerator();

        rebate.setOperation(operationBack);
        assertThat(rebate.getOperation()).isEqualTo(operationBack);

        rebate.operation(null);
        assertThat(rebate.getOperation()).isNull();
    }
}
