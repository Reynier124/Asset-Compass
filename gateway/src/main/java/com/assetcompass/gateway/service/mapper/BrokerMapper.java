package com.assetcompass.gateway.service.mapper;

import com.assetcompass.gateway.domain.Broker;
import com.assetcompass.gateway.service.dto.BrokerDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Broker} and its DTO {@link BrokerDTO}.
 */
@Mapper(componentModel = "spring")
public interface BrokerMapper extends EntityMapper<BrokerDTO, Broker> {}
