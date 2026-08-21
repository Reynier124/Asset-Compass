package com.assetcompass.portfolio.service;

import com.assetcompass.portfolio.domain.*; // for static metamodels
import com.assetcompass.portfolio.domain.Operation;
import com.assetcompass.portfolio.repository.OperationRepository;
import com.assetcompass.portfolio.service.criteria.OperationCriteria;
import com.assetcompass.portfolio.service.dto.OperationDTO;
import com.assetcompass.portfolio.service.mapper.OperationMapper;
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
 * Service for executing complex queries for {@link Operation} entities in the database.
 * The main input is a {@link OperationCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link OperationDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class OperationQueryService extends QueryService<Operation> {

    private static final Logger LOG = LoggerFactory.getLogger(OperationQueryService.class);

    private final OperationRepository operationRepository;

    private final OperationMapper operationMapper;

    public OperationQueryService(OperationRepository operationRepository, OperationMapper operationMapper) {
        this.operationRepository = operationRepository;
        this.operationMapper = operationMapper;
    }

    /**
     * Return a {@link Page} of {@link OperationDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<OperationDTO> findByCriteria(OperationCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Operation> specification = createSpecification(criteria);
        return operationRepository.findAll(specification, page).map(operationMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(OperationCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Operation> specification = createSpecification(criteria);
        return operationRepository.count(specification);
    }

    /**
     * Function to convert {@link OperationCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Operation> createSpecification(OperationCriteria criteria) {
        Specification<Operation> specification = Specification.unrestricted();
        specification = specification.and((root, query, builder) -> {
            if (Long.class != query.getResultType()) {
                root.fetch(Operation_.account, JoinType.LEFT);
                root.fetch(Operation_.asset, JoinType.LEFT);
                root.fetch(Operation_.closesOperation, JoinType.LEFT);
            }
            return null;
        });
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = specification.and(
                Specification.allOf(
                    Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                    buildSpecification(criteria.getId(), Operation_.id),
                    buildSpecification(criteria.getType(), Operation_.type),
                    buildRangeSpecification(criteria.getOperationDate(), Operation_.operationDate),
                    buildRangeSpecification(criteria.getQuantity(), Operation_.quantity),
                    buildRangeSpecification(criteria.getPrice(), Operation_.price),
                    buildRangeSpecification(criteria.getAmount(), Operation_.amount),
                    buildStringSpecification(criteria.getCurrency(), Operation_.currency),
                    buildRangeSpecification(criteria.getUnderlyingPrice(), Operation_.underlyingPrice),
                    buildRangeSpecification(criteria.getCommission(), Operation_.commission),
                    buildSpecification(criteria.getAccountId(), root ->
                        root.join(Operation_.account, JoinType.LEFT).get(BrokerAccount_.id)
                    ),
                    buildSpecification(criteria.getAssetId(), root -> root.join(Operation_.asset, JoinType.LEFT).get(Asset_.id)),
                    buildSpecification(criteria.getClosesOperationId(), root ->
                        root.join(Operation_.closesOperation, JoinType.LEFT).get(Operation_.id)
                    )
                )
            );
        }
        return specification;
    }
}
