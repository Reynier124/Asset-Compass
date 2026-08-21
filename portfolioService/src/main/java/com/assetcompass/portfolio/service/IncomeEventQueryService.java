package com.assetcompass.portfolio.service;

import com.assetcompass.portfolio.domain.*; // for static metamodels
import com.assetcompass.portfolio.domain.IncomeEvent;
import com.assetcompass.portfolio.repository.IncomeEventRepository;
import com.assetcompass.portfolio.service.criteria.IncomeEventCriteria;
import com.assetcompass.portfolio.service.dto.IncomeEventDTO;
import com.assetcompass.portfolio.service.mapper.IncomeEventMapper;
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
 * Service for executing complex queries for {@link IncomeEvent} entities in the database.
 * The main input is a {@link IncomeEventCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link IncomeEventDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class IncomeEventQueryService extends QueryService<IncomeEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(IncomeEventQueryService.class);

    private final IncomeEventRepository incomeEventRepository;

    private final IncomeEventMapper incomeEventMapper;

    public IncomeEventQueryService(IncomeEventRepository incomeEventRepository, IncomeEventMapper incomeEventMapper) {
        this.incomeEventRepository = incomeEventRepository;
        this.incomeEventMapper = incomeEventMapper;
    }

    /**
     * Return a {@link Page} of {@link IncomeEventDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<IncomeEventDTO> findByCriteria(IncomeEventCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<IncomeEvent> specification = createSpecification(criteria);
        return incomeEventRepository.findAll(specification, page).map(incomeEventMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(IncomeEventCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<IncomeEvent> specification = createSpecification(criteria);
        return incomeEventRepository.count(specification);
    }

    /**
     * Function to convert {@link IncomeEventCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<IncomeEvent> createSpecification(IncomeEventCriteria criteria) {
        Specification<IncomeEvent> specification = Specification.unrestricted();
        specification = specification.and((root, query, builder) -> {
            if (Long.class != query.getResultType()) {
                root.fetch(IncomeEvent_.account, JoinType.LEFT);
                root.fetch(IncomeEvent_.asset, JoinType.LEFT);
            }
            return null;
        });
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = specification.and(
                Specification.allOf(
                    Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                    buildSpecification(criteria.getId(), IncomeEvent_.id),
                    buildSpecification(criteria.getType(), IncomeEvent_.type),
                    buildRangeSpecification(criteria.getEventDate(), IncomeEvent_.eventDate),
                    buildRangeSpecification(criteria.getAmount(), IncomeEvent_.amount),
                    buildStringSpecification(criteria.getCurrency(), IncomeEvent_.currency),
                    buildSpecification(criteria.getAccountId(), root ->
                        root.join(IncomeEvent_.account, JoinType.LEFT).get(BrokerAccount_.id)
                    ),
                    buildSpecification(criteria.getAssetId(), root -> root.join(IncomeEvent_.asset, JoinType.LEFT).get(Asset_.id))
                )
            );
        }
        return specification;
    }
}
