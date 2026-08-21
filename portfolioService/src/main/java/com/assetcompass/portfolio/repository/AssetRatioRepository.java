package com.assetcompass.portfolio.repository;

import com.assetcompass.portfolio.domain.AssetRatio;
import java.util.UUID;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the AssetRatio entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AssetRatioRepository extends JpaRepository<AssetRatio, UUID>, JpaSpecificationExecutor<AssetRatio> {}
