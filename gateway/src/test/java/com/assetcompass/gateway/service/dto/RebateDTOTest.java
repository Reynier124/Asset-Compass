package com.assetcompass.gateway.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.assetcompass.gateway.web.rest.TestUtil;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RebateDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(RebateDTO.class);
        RebateDTO rebateDTO1 = new RebateDTO();
        rebateDTO1.setId(UUID.randomUUID());
        RebateDTO rebateDTO2 = new RebateDTO();
        assertThat(rebateDTO1).isNotEqualTo(rebateDTO2);
        rebateDTO2.setId(rebateDTO1.getId());
        assertThat(rebateDTO1).isEqualTo(rebateDTO2);
        rebateDTO2.setId(UUID.randomUUID());
        assertThat(rebateDTO1).isNotEqualTo(rebateDTO2);
        rebateDTO1.setId(null);
        assertThat(rebateDTO1).isNotEqualTo(rebateDTO2);
    }
}
