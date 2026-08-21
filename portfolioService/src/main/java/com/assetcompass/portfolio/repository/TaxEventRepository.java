package com.assetcompass.portfolio.repository;

import com.assetcompass.portfolio.domain.TaxEvent;
import java.util.UUID;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the TaxEvent entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TaxEventRepository extends JpaRepository<TaxEvent, UUID>, JpaSpecificationExecutor<TaxEvent> {}
