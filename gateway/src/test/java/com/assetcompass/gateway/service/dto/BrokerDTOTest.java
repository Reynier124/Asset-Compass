package com.assetcompass.gateway.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.assetcompass.gateway.web.rest.TestUtil;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class BrokerDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(BrokerDTO.class);
        BrokerDTO brokerDTO1 = new BrokerDTO();
        brokerDTO1.setId(UUID.randomUUID());
        BrokerDTO brokerDTO2 = new BrokerDTO();
        assertThat(brokerDTO1).isNotEqualTo(brokerDTO2);
        brokerDTO2.setId(brokerDTO1.getId());
        assertThat(brokerDTO1).isEqualTo(brokerDTO2);
        brokerDTO2.setId(UUID.randomUUID());
        assertThat(brokerDTO1).isNotEqualTo(brokerDTO2);
        brokerDTO1.setId(null);
        assertThat(brokerDTO1).isNotEqualTo(brokerDTO2);
    }
}
