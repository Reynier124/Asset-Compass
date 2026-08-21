package com.assetcompass.portfolio.repository;

import com.assetcompass.portfolio.domain.Broker;
import java.util.UUID;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Broker entity.
 */
@SuppressWarnings("unused")
@Repository
public interface BrokerRepository extends JpaRepository<Broker, UUID>, JpaSpecificationExecutor<Broker> {}
