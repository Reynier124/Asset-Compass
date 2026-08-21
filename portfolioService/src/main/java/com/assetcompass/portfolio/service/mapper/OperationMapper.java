package com.assetcompass.portfolio.service.mapper;

import com.assetcompass.portfolio.domain.Asset;
import com.assetcompass.portfolio.domain.BrokerAccount;
import com.assetcompass.portfolio.domain.Operation;
import com.assetcompass.portfolio.service.dto.AssetDTO;
import com.assetcompass.portfolio.service.dto.BrokerAccountDTO;
import com.assetcompass.portfolio.service.dto.OperationDTO;
import java.util.Objects;
import java.util.UUID;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Operation} and its DTO {@link OperationDTO}.
 */
@Mapper(componentModel = "spring")
public interface OperationMapper extends EntityMapper<OperationDTO, Operation> {
    @Mapping(target = "account", source = "account", qualifiedByName = "brokerAccountId")
    @Mapping(target = "asset", source = "asset", qualifiedByName = "assetId")
    @Mapping(target = "closesOperation", source = "closesOperation", qualifiedByName = "operationId")
    OperationDTO toDto(Operation s);

    @Named("brokerAccountId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    BrokerAccountDTO toDtoBrokerAccountId(BrokerAccount brokerAccount);

    @Named("assetId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    AssetDTO toDtoAssetId(Asset asset);

    @Named("operationId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    OperationDTO toDtoOperationId(Operation operation);

    default String map(UUID value) {
        return Objects.toString(value, null);
    }
}
