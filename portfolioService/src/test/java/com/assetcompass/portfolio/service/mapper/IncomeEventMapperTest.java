package com.assetcompass.portfolio.service.mapper;

import static com.assetcompass.portfolio.domain.IncomeEventAsserts.*;
import static com.assetcompass.portfolio.domain.IncomeEventTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IncomeEventMapperTest {

    private IncomeEventMapper incomeEventMapper;

    @BeforeEach
    void setUp() {
        incomeEventMapper = new IncomeEventMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getIncomeEventSample1();
        var actual = incomeEventMapper.toEntity(incomeEventMapper.toDto(expected));
        assertIncomeEventAllPropertiesEquals(expected, actual);
    }
}
