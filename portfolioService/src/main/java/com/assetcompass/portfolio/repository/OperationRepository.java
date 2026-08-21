package com.assetcompass.portfolio.repository;

import com.assetcompass.portfolio.domain.Operation;
import java.util.UUID;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Operation entity.
 */
@SuppressWarnings("unused")
@Repository
public interface OperationRepository extends JpaRepository<Operation, UUID>, JpaSpecificationExecutor<Operation> {}
