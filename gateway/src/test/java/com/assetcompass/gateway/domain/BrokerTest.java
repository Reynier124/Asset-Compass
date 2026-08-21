package com.assetcompass.gateway.domain;

import static com.assetcompass.gateway.domain.BrokerTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.assetcompass.gateway.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class BrokerTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Broker.class);
        Broker broker1 = getBrokerSample1();
        Broker broker2 = new Broker();
        assertThat(broker1).isNotEqualTo(broker2);

        broker2.setId(broker1.getId());
        assertThat(broker1).isEqualTo(broker2);

        broker2 = getBrokerSample2();
        assertThat(broker1).isNotEqualTo(broker2);
    }
}
