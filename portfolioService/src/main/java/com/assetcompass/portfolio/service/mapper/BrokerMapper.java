package com.assetcompass.portfolio.service.mapper;

import com.assetcompass.portfolio.domain.Broker;
import com.assetcompass.portfolio.service.dto.BrokerDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Broker} and its DTO {@link BrokerDTO}.
 */
@Mapper(componentModel = "spring")
public interface BrokerMapper extends EntityMapper<BrokerDTO, Broker> {}
