package com.assetcompass.gateway.domain;

import static com.assetcompass.gateway.domain.BrokerAccountTestSamples.*;
import static com.assetcompass.gateway.domain.IncomeEventTestSamples.*;
import static com.assetcompass.gateway.domain.OperationTestSamples.*;
import static com.assetcompass.gateway.domain.TaxEventTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.assetcompass.gateway.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class TaxEventTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(TaxEvent.class);
        TaxEvent taxEvent1 = getTaxEventSample1();
        TaxEvent taxEvent2 = new TaxEvent();
        assertThat(taxEvent1).isNotEqualTo(taxEvent2);

        taxEvent2.setId(taxEvent1.getId());
        assertThat(taxEvent1).isEqualTo(taxEvent2);

        taxEvent2 = getTaxEventSample2();
        assertThat(taxEvent1).isNotEqualTo(taxEvent2);
    }

    @Test
    void accountTest() {
        TaxEvent taxEvent = getTaxEventRandomSampleGenerator();
        BrokerAccount brokerAccountBack = getBrokerAccountRandomSampleGenerator();

        taxEvent.setAccount(brokerAccountBack);
        assertThat(taxEvent.getAccount()).isEqualTo(brokerAccountBack);

        taxEvent.account(null);
        assertThat(taxEvent.getAccount()).isNull();
    }

    @Test
    void operationTest() {
        TaxEvent taxEvent = getTaxEventRandomSampleGenerator();
        Operation operationBack = getOperationRandomSampleGenerator();

        taxEvent.setOperation(operationBack);
        assertThat(taxEvent.getOperation()).isEqualTo(operationBack);

        taxEvent.operation(null);
        assertThat(taxEvent.getOperation()).isNull();
    }

    @Test
    void incomeEventTest() {
        TaxEvent taxEvent = getTaxEventRandomSampleGenerator();
        IncomeEvent incomeEventBack = getIncomeEventRandomSampleGenerator();

        taxEvent.setIncomeEvent(incomeEventBack);
        assertThat(taxEvent.getIncomeEvent()).isEqualTo(incomeEventBack);

        taxEvent.incomeEvent(null);
        assertThat(taxEvent.getIncomeEvent()).isNull();
    }
}
