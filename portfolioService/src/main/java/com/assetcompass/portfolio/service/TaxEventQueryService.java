package com.assetcompass.portfolio.service;

import com.assetcompass.portfolio.domain.*; // for static metamodels
import com.assetcompass.portfolio.domain.TaxEvent;
import com.assetcompass.portfolio.repository.TaxEventRepository;
import com.assetcompass.portfolio.service.criteria.TaxEventCriteria;
import com.assetcompass.portfolio.service.dto.TaxEventDTO;
import com.assetcompass.portfolio.service.mapper.TaxEventMapper;
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
 * Service for executing complex queries for {@link TaxEvent} entities in the database.
 * The main input is a {@link TaxEventCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link TaxEventDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class TaxEventQueryService extends QueryService<TaxEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(TaxEventQueryService.class);

    private final TaxEventRepository taxEventRepository;

    private final TaxEventMapper taxEventMapper;

    public TaxEventQueryService(TaxEventRepository taxEventRepository, TaxEventMapper taxEventMapper) {
        this.taxEventRepository = taxEventRepository;
        this.taxEventMapper = taxEventMapper;
    }

    /**
     * Return a {@link Page} of {@link TaxEventDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<TaxEventDTO> findByCriteria(TaxEventCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<TaxEvent> specification = createSpecification(criteria);
        return taxEventRepository.findAll(specification, page).map(taxEventMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(TaxEventCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<TaxEvent> specification = createSpecification(criteria);
        return taxEventRepository.count(specification);
    }

    /**
     * Function to convert {@link TaxEventCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<TaxEvent> createSpecification(TaxEventCriteria criteria) {
        Specification<TaxEvent> specification = Specification.unrestricted();
        specification = specification.and((root, query, builder) -> {
            if (Long.class != query.getResultType()) {
                root.fetch(TaxEvent_.account, JoinType.LEFT);
                root.fetch(TaxEvent_.operation, JoinType.LEFT);
                root.fetch(TaxEvent_.incomeEvent, JoinType.LEFT);
            }
            return null;
        });
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = specification.and(
                Specification.allOf(
                    Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                    buildSpecification(criteria.getId(), TaxEvent_.id),
                    buildSpecification(criteria.getType(), TaxEvent_.type),
                    buildRangeSpecification(criteria.getTaxDate(), TaxEvent_.taxDate),
                    buildRangeSpecification(criteria.getAmount(), TaxEvent_.amount),
                    buildStringSpecification(criteria.getCurrency(), TaxEvent_.currency),
                    buildSpecification(criteria.getAccountId(), root -> root.join(TaxEvent_.account, JoinType.LEFT).get(BrokerAccount_.id)),
                    buildSpecification(criteria.getOperationId(), root -> root.join(TaxEvent_.operation, JoinType.LEFT).get(Operation_.id)),
                    buildSpecification(criteria.getIncomeEventId(), root ->
                        root.join(TaxEvent_.incomeEvent, JoinType.LEFT).get(IncomeEvent_.id)
                    )
                )
            );
        }
        return specification;
    }
}
