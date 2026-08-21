package com.assetcompass.portfolio.repository;

import com.assetcompass.portfolio.domain.BrokerAccount;
import java.util.UUID;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the BrokerAccount entity.
 */
@SuppressWarnings("unused")
@Repository
public interface BrokerAccountRepository extends JpaRepository<BrokerAccount, UUID>, JpaSpecificationExecutor<BrokerAccount> {}
