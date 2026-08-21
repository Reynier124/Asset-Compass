package com.assetcompass.portfolio.repository;

import com.assetcompass.portfolio.domain.IncomeEvent;
import java.util.UUID;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the IncomeEvent entity.
 */
@SuppressWarnings("unused")
@Repository
public interface IncomeEventRepository extends JpaRepository<IncomeEvent, UUID>, JpaSpecificationExecutor<IncomeEvent> {}
