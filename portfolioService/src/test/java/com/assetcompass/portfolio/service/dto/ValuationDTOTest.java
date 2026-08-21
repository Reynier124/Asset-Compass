package com.assetcompass.portfolio.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.assetcompass.portfolio.web.rest.TestUtil;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ValuationDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(ValuationDTO.class);
        ValuationDTO valuationDTO1 = new ValuationDTO();
        valuationDTO1.setId(UUID.randomUUID());
        ValuationDTO valuationDTO2 = new ValuationDTO();
        assertThat(valuationDTO1).isNotEqualTo(valuationDTO2);
        valuationDTO2.setId(valuationDTO1.getId());
        assertThat(valuationDTO1).isEqualTo(valuationDTO2);
        valuationDTO2.setId(UUID.randomUUID());
        assertThat(valuationDTO1).isNotEqualTo(valuationDTO2);
        valuationDTO1.setId(null);
        assertThat(valuationDTO1).isNotEqualTo(valuationDTO2);
    }
}
