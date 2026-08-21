package com.assetcompass.gateway.service.mapper;

import com.assetcompass.gateway.domain.Broker;
import com.assetcompass.gateway.domain.BrokerAccount;
import com.assetcompass.gateway.service.dto.BrokerAccountDTO;
import com.assetcompass.gateway.service.dto.BrokerDTO;
import java.util.Objects;
import java.util.UUID;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link BrokerAccount} and its DTO {@link BrokerAccountDTO}.
 */
@Mapper(componentModel = "spring")
public interface BrokerAccountMapper extends EntityMapper<BrokerAccountDTO, BrokerAccount> {
    @Mapping(target = "broker", source = "broker", qualifiedByName = "brokerId")
    BrokerAccountDTO toDto(BrokerAccount s);

    @Named("brokerId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    BrokerDTO toDtoBrokerId(Broker broker);

    default String map(UUID value) {
        return Objects.toString(value, null);
    }
}
