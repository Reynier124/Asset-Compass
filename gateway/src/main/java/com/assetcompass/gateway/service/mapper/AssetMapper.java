package com.assetcompass.gateway.service.mapper;

import com.assetcompass.gateway.domain.Asset;
import com.assetcompass.gateway.service.dto.AssetDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Asset} and its DTO {@link AssetDTO}.
 */
@Mapper(componentModel = "spring")
public interface AssetMapper extends EntityMapper<AssetDTO, Asset> {}
