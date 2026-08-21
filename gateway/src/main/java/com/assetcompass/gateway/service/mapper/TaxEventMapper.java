package com.assetcompass.gateway.service.mapper;

import com.assetcompass.gateway.domain.BrokerAccount;
import com.assetcompass.gateway.domain.IncomeEvent;
import com.assetcompass.gateway.domain.Operation;
import com.assetcompass.gateway.domain.TaxEvent;
import com.assetcompass.gateway.service.dto.BrokerAccountDTO;
import com.assetcompass.gateway.service.dto.IncomeEventDTO;
import com.assetcompass.gateway.service.dto.OperationDTO;
import com.assetcompass.gateway.service.dto.TaxEventDTO;
import java.util.Objects;
import java.util.UUID;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link TaxEvent} and its DTO {@link TaxEventDTO}.
 */
@Mapper(componentModel = "spring")
public interface TaxEventMapper extends EntityMapper<TaxEventDTO, TaxEvent> {
    @Mapping(target = "account", source = "account", qualifiedByName = "brokerAccountId")
    @Mapping(target = "operation", source = "operation", qualifiedByName = "operationId")
    @Mapping(target = "incomeEvent", source = "incomeEvent", qualifiedByName = "incomeEventId")
    TaxEventDTO toDto(TaxEvent s);

    @Named("brokerAccountId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    BrokerAccountDTO toDtoBrokerAccountId(BrokerAccount brokerAccount);

    @Named("operationId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    OperationDTO toDtoOperationId(Operation operation);

    @Named("incomeEventId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    IncomeEventDTO toDtoIncomeEventId(IncomeEvent incomeEvent);

    default String map(UUID value) {
        return Objects.toString(value, null);
    }
}
