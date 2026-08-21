package com.assetcompass.gateway.service.mapper;

import com.assetcompass.gateway.domain.BrokerAccount;
import com.assetcompass.gateway.domain.Operation;
import com.assetcompass.gateway.domain.Rebate;
import com.assetcompass.gateway.service.dto.BrokerAccountDTO;
import com.assetcompass.gateway.service.dto.OperationDTO;
import com.assetcompass.gateway.service.dto.RebateDTO;
import java.util.Objects;
import java.util.UUID;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Rebate} and its DTO {@link RebateDTO}.
 */
@Mapper(componentModel = "spring")
public interface RebateMapper extends EntityMapper<RebateDTO, Rebate> {
    @Mapping(target = "account", source = "account", qualifiedByName = "brokerAccountId")
    @Mapping(target = "operation", source = "operation", qualifiedByName = "operationId")
    RebateDTO toDto(Rebate s);

    @Named("brokerAccountId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    BrokerAccountDTO toDtoBrokerAccountId(BrokerAccount brokerAccount);

    @Named("operationId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    OperationDTO toDtoOperationId(Operation operation);

    default String map(UUID value) {
        return Objects.toString(value, null);
    }
}
