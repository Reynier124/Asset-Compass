package com.assetcompass.portfolio.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.assetcompass.portfolio.web.rest.TestUtil;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TaxEventDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(TaxEventDTO.class);
        TaxEventDTO taxEventDTO1 = new TaxEventDTO();
        taxEventDTO1.setId(UUID.randomUUID());
        TaxEventDTO taxEventDTO2 = new TaxEventDTO();
        assertThat(taxEventDTO1).isNotEqualTo(taxEventDTO2);
        taxEventDTO2.setId(taxEventDTO1.getId());
        assertThat(taxEventDTO1).isEqualTo(taxEventDTO2);
        taxEventDTO2.setId(UUID.randomUUID());
        assertThat(taxEventDTO1).isNotEqualTo(taxEventDTO2);
        taxEventDTO1.setId(null);
        assertThat(taxEventDTO1).isNotEqualTo(taxEventDTO2);
    }
}
