package com.assetcompass.portfolio.service.mapper;

import static com.assetcompass.portfolio.domain.OperationAsserts.*;
import static com.assetcompass.portfolio.domain.OperationTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OperationMapperTest {

    private OperationMapper operationMapper;

    @BeforeEach
    void setUp() {
        operationMapper = new OperationMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getOperationSample1();
        var actual = operationMapper.toEntity(operationMapper.toDto(expected));
        assertOperationAllPropertiesEquals(expected, actual);
    }
}
