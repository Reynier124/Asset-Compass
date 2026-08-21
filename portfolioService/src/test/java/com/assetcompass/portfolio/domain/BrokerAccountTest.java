package com.assetcompass.portfolio.domain;

import static com.assetcompass.portfolio.domain.BrokerAccountTestSamples.*;
import static com.assetcompass.portfolio.domain.BrokerTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.assetcompass.portfolio.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class BrokerAccountTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(BrokerAccount.class);
        BrokerAccount brokerAccount1 = getBrokerAccountSample1();
        BrokerAccount brokerAccount2 = new BrokerAccount();
        assertThat(brokerAccount1).isNotEqualTo(brokerAccount2);

        brokerAccount2.setId(brokerAccount1.getId());
        assertThat(brokerAccount1).isEqualTo(brokerAccount2);

        brokerAccount2 = getBrokerAccountSample2();
        assertThat(brokerAccount1).isNotEqualTo(brokerAccount2);
    }

    @Test
    void brokerTest() {
        BrokerAccount brokerAccount = getBrokerAccountRandomSampleGenerator();
        Broker brokerBack = getBrokerRandomSampleGenerator();

        brokerAccount.setBroker(brokerBack);
        assertThat(brokerAccount.getBroker()).isEqualTo(brokerBack);

        brokerAccount.broker(null);
        assertThat(brokerAccount.getBroker()).isNull();
    }
}
