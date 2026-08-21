package com.assetcompass.portfolio.service;

import com.assetcompass.portfolio.domain.*; // for static metamodels
import com.assetcompass.portfolio.domain.Valuation;
import com.assetcompass.portfolio.repository.ValuationRepository;
import com.assetcompass.portfolio.service.criteria.ValuationCriteria;
import com.assetcompass.portfolio.service.dto.ValuationDTO;
import com.assetcompass.portfolio.service.mapper.ValuationMapper;
import jakarta.persistence.criteria.JoinType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link Valuation} entities in the database.
 * The main input is a {@link ValuationCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link ValuationDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class ValuationQueryService extends QueryService<Valuation> {

    private static final Logger LOG = LoggerFactory.getLogger(ValuationQueryService.class);

    private final ValuationRepository valuationRepository;

    private final ValuationMapper valuationMapper;

    public ValuationQueryService(ValuationRepository valuationRepository, ValuationMapper valuationMapper) {
        this.valuationRepository = valuationRepository;
        this.valuationMapper = valuationMapper;
    }

    /**
     * Return a {@link Page} of {@link ValuationDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<ValuationDTO> findByCriteria(ValuationCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Valuation> specification = createSpecification(criteria);
        return valuationRepository.findAll(specification, page).map(valuationMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(ValuationCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Valuation> specification = createSpecification(criteria);
        return valuationRepository.count(specification);
    }

    /**
     * Function to convert {@link ValuationCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Valuation> createSpecification(ValuationCriteria criteria) {
        Specification<Valuation> specification = Specification.unrestricted();
        specification = specification.and((root, query, builder) -> {
            if (Long.class != query.getResultType()) {
                root.fetch(Valuation_.account, JoinType.LEFT);
            }
            return null;
        });
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = specification.and(
                Specification.allOf(
                    Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                    buildSpecification(criteria.getId(), Valuation_.id),
                    buildRangeSpecification(criteria.getSnapshotDate(), Valuation_.snapshotDate),
                    buildRangeSpecification(criteria.getTotalValue(), Valuation_.totalValue),
                    buildStringSpecification(criteria.getCurrency(), Valuation_.currency),
                    buildSpecification(criteria.getAccountId(), root -> root.join(Valuation_.account, JoinType.LEFT).get(BrokerAccount_.id))
                )
            );
        }
        return specification;
    }
}
