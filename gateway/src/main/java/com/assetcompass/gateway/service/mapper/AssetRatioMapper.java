package com.assetcompass.gateway.service.mapper;

import com.assetcompass.gateway.domain.Asset;
import com.assetcompass.gateway.domain.AssetRatio;
import com.assetcompass.gateway.service.dto.AssetDTO;
import com.assetcompass.gateway.service.dto.AssetRatioDTO;
import java.util.Objects;
import java.util.UUID;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link AssetRatio} and its DTO {@link AssetRatioDTO}.
 */
@Mapper(componentModel = "spring")
public interface AssetRatioMapper extends EntityMapper<AssetRatioDTO, AssetRatio> {
    @Mapping(target = "asset", source = "asset", qualifiedByName = "assetId")
    AssetRatioDTO toDto(AssetRatio s);

    @Named("assetId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    AssetDTO toDtoAssetId(Asset asset);

    default String map(UUID value) {
        return Objects.toString(value, null);
    }
}
