package com.assetcompass.portfolio.repository;

import com.assetcompass.portfolio.domain.Rebate;
import java.util.UUID;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Rebate entity.
 */
@SuppressWarnings("unused")
@Repository
public interface RebateRepository extends JpaRepository<Rebate, UUID>, JpaSpecificationExecutor<Rebate> {}
