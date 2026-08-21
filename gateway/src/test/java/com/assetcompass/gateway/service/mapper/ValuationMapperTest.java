package com.assetcompass.gateway.service.mapper;

import static com.assetcompass.gateway.domain.ValuationAsserts.*;
import static com.assetcompass.gateway.domain.ValuationTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ValuationMapperTest {

    private ValuationMapper valuationMapper;

    @BeforeEach
    void setUp() {
        valuationMapper = new ValuationMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getValuationSample1();
        var actual = valuationMapper.toEntity(valuationMapper.toDto(expected));
        assertValuationAllPropertiesEquals(expected, actual);
    }
}
