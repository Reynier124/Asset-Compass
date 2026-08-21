package com.assetcompass.portfolio.service.mapper;

import com.assetcompass.portfolio.domain.Asset;
import com.assetcompass.portfolio.domain.BrokerAccount;
import com.assetcompass.portfolio.domain.IncomeEvent;
import com.assetcompass.portfolio.service.dto.AssetDTO;
import com.assetcompass.portfolio.service.dto.BrokerAccountDTO;
import com.assetcompass.portfolio.service.dto.IncomeEventDTO;
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
