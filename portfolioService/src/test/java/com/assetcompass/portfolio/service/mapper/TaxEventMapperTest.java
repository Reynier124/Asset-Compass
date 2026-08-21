package com.assetcompass.portfolio.service.mapper;

import static com.assetcompass.portfolio.domain.TaxEventAsserts.*;
import static com.assetcompass.portfolio.domain.TaxEventTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TaxEventMapperTest {

    private TaxEventMapper taxEventMapper;

    @BeforeEach
    void setUp() {
        taxEventMapper = new TaxEventMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getTaxEventSample1();
        var actual = taxEventMapper.toEntity(taxEventMapper.toDto(expected));
        assertTaxEventAllPropertiesEquals(expected, actual);
    }
}
