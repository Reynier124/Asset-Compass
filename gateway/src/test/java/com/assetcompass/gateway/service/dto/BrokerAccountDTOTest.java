package com.assetcompass.gateway.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.assetcompass.gateway.web.rest.TestUtil;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class BrokerAccountDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(BrokerAccountDTO.class);
        BrokerAccountDTO brokerAccountDTO1 = new BrokerAccountDTO();
        brokerAccountDTO1.setId(UUID.randomUUID());
        BrokerAccountDTO brokerAccountDTO2 = new BrokerAccountDTO();
        assertThat(brokerAccountDTO1).isNotEqualTo(brokerAccountDTO2);
        brokerAccountDTO2.setId(brokerAccountDTO1.getId());
        assertThat(brokerAccountDTO1).isEqualTo(brokerAccountDTO2);
        brokerAccountDTO2.setId(UUID.randomUUID());
        assertThat(brokerAccountDTO1).isNotEqualTo(brokerAccountDTO2);
        brokerAccountDTO1.setId(null);
        assertThat(brokerAccountDTO1).isNotEqualTo(brokerAccountDTO2);
    }
}
