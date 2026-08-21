package com.assetcompass.portfolio.repository;

import com.assetcompass.portfolio.domain.Valuation;
import java.util.UUID;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Valuation entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ValuationRepository extends JpaRepository<Valuation, UUID>, JpaSpecificationExecutor<Valuation> {}
