package com.assetcompass.gateway.service.mapper;

import com.assetcompass.gateway.domain.BrokerAccount;
import com.assetcompass.gateway.domain.Valuation;
import com.assetcompass.gateway.service.dto.BrokerAccountDTO;
import com.assetcompass.gateway.service.dto.ValuationDTO;
import java.util.Objects;
import java.util.UUID;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Valuation} and its DTO {@link ValuationDTO}.
 */
@Mapper(componentModel = "spring")
public interface ValuationMapper extends EntityMapper<ValuationDTO, Valuation> {
    @Mapping(target = "account", source = "account", qualifiedByName = "brokerAccountId")
    ValuationDTO toDto(Valuation s);

    @Named("brokerAccountId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    BrokerAccountDTO toDtoBrokerAccountId(BrokerAccount brokerAccount);

    default String map(UUID value) {
        return Objects.toString(value, null);
    }
}
