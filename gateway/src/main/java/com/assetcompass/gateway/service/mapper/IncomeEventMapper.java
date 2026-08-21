package com.assetcompass.gateway.service.mapper;

import com.assetcompass.gateway.domain.Asset;
import com.assetcompass.gateway.domain.BrokerAccount;
import com.assetcompass.gateway.domain.IncomeEvent;
import com.assetcompass.gateway.service.dto.AssetDTO;
import com.assetcompass.gateway.service.dto.BrokerAccountDTO;
import com.assetcompass.gateway.service.dto.IncomeEventDTO;
import java.util.Objects;
import java.util.UUID;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link IncomeEvent} and its DTO {@link IncomeEventDTO}.
 */
@Mapper(componentModel = "spring")
public interface IncomeEventMapper extends EntityMapper<IncomeEventDTO, IncomeEvent> {
    @Mapping(target = "account", source = "account", qualifiedByName = "brokerAccountId")
    @Mapping(target = "asset", source = "asset", qualifiedByName = "assetId")
    IncomeEventDTO toDto(IncomeEvent s);

    @Named("brokerAccountId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    BrokerAccountDTO toDtoBrokerAccountId(BrokerAccount brokerAccount);

    @Named("assetId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    AssetDTO toDtoAssetId(Asset asset);

    default String map(UUID value) {
        return Objects.toString(value, null);
    }
}
