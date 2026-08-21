package com.assetcompass.portfolio.service.mapper;

import static com.assetcompass.portfolio.domain.RebateAsserts.*;
import static com.assetcompass.portfolio.domain.RebateTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RebateMapperTest {

    private RebateMapper rebateMapper;

    @BeforeEach
    void setUp() {
        rebateMapper = new RebateMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getRebateSample1();
        var actual = rebateMapper.toEntity(rebateMapper.toDto(expected));
        assertRebateAllPropertiesEquals(expected, actual);
    }
}
