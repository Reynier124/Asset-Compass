package com.assetcompass.portfolio.service.mapper;

import static com.assetcompass.portfolio.domain.AssetRatioAsserts.*;
import static com.assetcompass.portfolio.domain.AssetRatioTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AssetRatioMapperTest {

    private AssetRatioMapper assetRatioMapper;

    @BeforeEach
    void setUp() {
        assetRatioMapper = new AssetRatioMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getAssetRatioSample1();
        var actual = assetRatioMapper.toEntity(assetRatioMapper.toDto(expected));
        assertAssetRatioAllPropertiesEquals(expected, actual);
    }
}
