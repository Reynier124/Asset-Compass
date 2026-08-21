package com.assetcompass.gateway.domain;

import static com.assetcompass.gateway.domain.AssetTestSamples.*;
import static com.assetcompass.gateway.domain.BrokerAccountTestSamples.*;
import static com.assetcompass.gateway.domain.OperationTestSamples.*;
import static com.assetcompass.gateway.domain.OperationTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.assetcompass.gateway.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class OperationTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Operation.class);
        Operation operation1 = getOperationSample1();
        Operation operation2 = new Operation();
        assertThat(operation1).isNotEqualTo(operation2);

        operation2.setId(operation1.getId());
        assertThat(operation1).isEqualTo(operation2);

        operation2 = getOperationSample2();
        assertThat(operation1).isNotEqualTo(operation2);
    }

    @Test
    void accountTest() {
        Operation operation = getOperationRandomSampleGenerator();
        BrokerAccount brokerAccountBack = getBrokerAccountRandomSampleGenerator();

        operation.setAccount(brokerAccountBack);
        assertThat(operation.getAccount()).isEqualTo(brokerAccountBack);

        operation.account(null);
        assertThat(operation.getAccount()).isNull();
    }

    @Test
    void assetTest() {
        Operation operation = getOperationRandomSampleGenerator();
        Asset assetBack = getAssetRandomSampleGenerator();

        operation.setAsset(assetBack);
        assertThat(operation.getAsset()).isEqualTo(assetBack);

        operation.asset(null);
        assertThat(operation.getAsset()).isNull();
    }

    @Test
    void closesOperationTest() {
        Operation operation = getOperationRandomSampleGenerator();
        Operation operationBack = getOperationRandomSampleGenerator();

        operation.setClosesOperation(operationBack);
        assertThat(operation.getClosesOperation()).isEqualTo(operationBack);

        operation.closesOperation(null);
        assertThat(operation.getClosesOperation()).isNull();
    }
}
