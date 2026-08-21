package com.assetcompass.gateway.service.mapper;

import static com.assetcompass.gateway.domain.BrokerAccountAsserts.*;
import static com.assetcompass.gateway.domain.BrokerAccountTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BrokerAccountMapperTest {

    private BrokerAccountMapper brokerAccountMapper;

    @BeforeEach
    void setUp() {
        brokerAccountMapper = new BrokerAccountMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getBrokerAccountSample1();
        var actual = brokerAccountMapper.toEntity(brokerAccountMapper.toDto(expected));
        assertBrokerAccountAllPropertiesEquals(expected, actual);
    }
}
