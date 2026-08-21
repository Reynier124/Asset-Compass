package com.assetcompass.portfolio.service;

import com.assetcompass.portfolio.domain.*; // for static metamodels
import com.assetcompass.portfolio.domain.BrokerAccount;
import com.assetcompass.portfolio.repository.BrokerAccountRepository;
import com.assetcompass.portfolio.service.criteria.BrokerAccountCriteria;
import com.assetcompass.portfolio.service.dto.BrokerAccountDTO;
import com.assetcompass.portfolio.service.mapper.BrokerAccountMapper;
import jakarta.persistence.criteria.JoinType;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link BrokerAccount} entities in the database.
 * The main input is a {@link BrokerAccountCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link BrokerAccountDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class BrokerAccountQueryService extends QueryService<BrokerAccount> {

    private static final Logger LOG = LoggerFactory.getLogger(BrokerAccountQueryService.class);

    private final BrokerAccountRepository brokerAccountRepository;

    private final BrokerAccountMapper brokerAccountMapper;

    public BrokerAccountQueryService(BrokerAccountRepository brokerAccountRepository, BrokerAccountMapper brokerAccountMapper) {
        this.brokerAccountRepository = brokerAccountRepository;
        this.brokerAccountMapper = brokerAccountMapper;
    }

    /**
     * Return a {@link List} of {@link BrokerAccountDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<BrokerAccountDTO> findByCriteria(BrokerAccountCriteria criteria) {
        LOG.debug("find by criteria : {}", criteria);
        final Specification<BrokerAccount> specification = createSpecification(criteria);
        return brokerAccountMapper.toDto(brokerAccountRepository.findAll(specification));
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(BrokerAccountCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<BrokerAccount> specification = createSpecification(criteria);
        return brokerAccountRepository.count(specification);
    }

    /**
     * Function to convert {@link BrokerAccountCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<BrokerAccount> createSpecification(BrokerAccountCriteria criteria) {
        Specification<BrokerAccount> specification = Specification.unrestricted();
        specification = specification.and((root, query, builder) -> {
            if (Long.class != query.getResultType()) {
                root.fetch(BrokerAccount_.broker, JoinType.LEFT);
            }
            return null;
        });
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = specification.and(
                Specification.allOf(
                    Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                    buildSpecification(criteria.getId(), BrokerAccount_.id),
                    buildStringSpecification(criteria.getExternalAccountId(), BrokerAccount_.externalAccountId),
                    buildStringSpecification(criteria.getDisplayName(), BrokerAccount_.displayName),
                    buildSpecification(criteria.getBrokerId(), root -> root.join(BrokerAccount_.broker, JoinType.LEFT).get(Broker_.id))
                )
            );
        }
        return specification;
    }
}
