package com.assetcompass.gateway.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.assetcompass.gateway.web.rest.TestUtil;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PositionDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(PositionDTO.class);
        PositionDTO positionDTO1 = new PositionDTO();
        positionDTO1.setId(UUID.randomUUID());
        PositionDTO positionDTO2 = new PositionDTO();
        assertThat(positionDTO1).isNotEqualTo(positionDTO2);
        positionDTO2.setId(positionDTO1.getId());
        assertThat(positionDTO1).isEqualTo(positionDTO2);
        positionDTO2.setId(UUID.randomUUID());
        assertThat(positionDTO1).isNotEqualTo(positionDTO2);
        positionDTO1.setId(null);
        assertThat(positionDTO1).isNotEqualTo(positionDTO2);
    }
}
