package com.assetcompass.portfolio.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.assetcompass.portfolio.web.rest.TestUtil;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class IncomeEventDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(IncomeEventDTO.class);
        IncomeEventDTO incomeEventDTO1 = new IncomeEventDTO();
        incomeEventDTO1.setId(UUID.randomUUID());
        IncomeEventDTO incomeEventDTO2 = new IncomeEventDTO();
        assertThat(incomeEventDTO1).isNotEqualTo(incomeEventDTO2);
        incomeEventDTO2.setId(incomeEventDTO1.getId());
        assertThat(incomeEventDTO1).isEqualTo(incomeEventDTO2);
        incomeEventDTO2.setId(UUID.randomUUID());
        assertThat(incomeEventDTO1).isNotEqualTo(incomeEventDTO2);
        incomeEventDTO1.setId(null);
        assertThat(incomeEventDTO1).isNotEqualTo(incomeEventDTO2);
    }
}
