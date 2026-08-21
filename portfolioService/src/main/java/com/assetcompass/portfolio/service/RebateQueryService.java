package com.assetcompass.portfolio.service;

import com.assetcompass.portfolio.domain.*; // for static metamodels
import com.assetcompass.portfolio.domain.Rebate;
import com.assetcompass.portfolio.repository.RebateRepository;
import com.assetcompass.portfolio.service.criteria.RebateCriteria;
import com.assetcompass.portfolio.service.dto.RebateDTO;
import com.assetcompass.portfolio.service.mapper.RebateMapper;
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
 * Service for executing complex queries for {@link Rebate} entities in the database.
 * The main input is a {@link RebateCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link RebateDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class RebateQueryService extends QueryService<Rebate> {

    private static final Logger LOG = LoggerFactory.getLogger(RebateQueryService.class);

    private final RebateRepository rebateRepository;

    private final RebateMapper rebateMapper;

    public RebateQueryService(RebateRepository rebateRepository, RebateMapper rebateMapper) {
        this.rebateRepository = rebateRepository;
        this.rebateMapper = rebateMapper;
    }

    /**
     * Return a {@link Page} of {@link RebateDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<RebateDTO> findByCriteria(RebateCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Rebate> specification = createSpecification(criteria);
        return rebateRepository.findAll(specification, page).map(rebateMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(RebateCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Rebate> specification = createSpecification(criteria);
        return rebateRepository.count(specification);
    }

    /**
     * Function to convert {@link RebateCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Rebate> createSpecification(RebateCriteria criteria) {
        Specification<Rebate> specification = Specification.unrestricted();
        specification = specification.and((root, query, builder) -> {
            if (Long.class != query.getResultType()) {
                root.fetch(Rebate_.account, JoinType.LEFT);
                root.fetch(Rebate_.operation, JoinType.LEFT);
            }
            return null;
        });
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = specification.and(
                Specification.allOf(
                    Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                    buildSpecification(criteria.getId(), Rebate_.id),
                    buildRangeSpecification(criteria.getRebateDate(), Rebate_.rebateDate),
                    buildRangeSpecification(criteria.getAmount(), Rebate_.amount),
                    buildStringSpecification(criteria.getCurrency(), Rebate_.currency),
                    buildSpecification(criteria.getAccountId(), root -> root.join(Rebate_.account, JoinType.LEFT).get(BrokerAccount_.id)),
                    buildSpecification(criteria.getOperationId(), root -> root.join(Rebate_.operation, JoinType.LEFT).get(Operation_.id))
                )
            );
        }
        return specification;
    }
}
