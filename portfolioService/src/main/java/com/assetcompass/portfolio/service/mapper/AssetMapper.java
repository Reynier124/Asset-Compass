package com.assetcompass.portfolio.service.mapper;

import com.assetcompass.portfolio.domain.Asset;
import com.assetcompass.portfolio.service.dto.AssetDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Asset} and its DTO {@link AssetDTO}.
 */
@Mapper(componentModel = "spring")
public interface AssetMapper extends EntityMapper<AssetDTO, Asset> {}
