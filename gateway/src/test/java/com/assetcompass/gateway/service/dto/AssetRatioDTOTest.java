package com.assetcompass.gateway.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.assetcompass.gateway.web.rest.TestUtil;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AssetRatioDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(AssetRatioDTO.class);
        AssetRatioDTO assetRatioDTO1 = new AssetRatioDTO();
        assetRatioDTO1.setId(UUID.randomUUID());
        AssetRatioDTO assetRatioDTO2 = new AssetRatioDTO();
        assertThat(assetRatioDTO1).isNotEqualTo(assetRatioDTO2);
        assetRatioDTO2.setId(assetRatioDTO1.getId());
        assertThat(assetRatioDTO1).isEqualTo(assetRatioDTO2);
        assetRatioDTO2.setId(UUID.randomUUID());
        assertThat(assetRatioDTO1).isNotEqualTo(assetRatioDTO2);
        assetRatioDTO1.setId(null);
        assertThat(assetRatioDTO1).isNotEqualTo(assetRatioDTO2);
    }
}
