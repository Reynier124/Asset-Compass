package com.assetcompass.portfolio.service;

import com.assetcompass.portfolio.domain.*; // for static metamodels
import com.assetcompass.portfolio.domain.Position;
import com.assetcompass.portfolio.repository.PositionRepository;
import com.assetcompass.portfolio.service.criteria.PositionCriteria;
import com.assetcompass.portfolio.service.dto.PositionDTO;
import com.assetcompass.portfolio.service.mapper.PositionMapper;
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
 * Service for executing complex queries for {@link Position} entities in the database.
 * The main input is a {@link PositionCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link PositionDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class PositionQueryService extends QueryService<Position> {

    private static final Logger LOG = LoggerFactory.getLogger(PositionQueryService.class);

    private final PositionRepository positionRepository;

    private final PositionMapper positionMapper;

    public PositionQueryService(PositionRepository positionRepository, PositionMapper positionMapper) {
        this.positionRepository = positionRepository;
        this.positionMapper = positionMapper;
    }

    /**
     * Return a {@link Page} of {@link PositionDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<PositionDTO> findByCriteria(PositionCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Position> specification = createSpecification(criteria);
        return positionRepository.findAll(specification, page).map(positionMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(PositionCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Position> specification = createSpecification(criteria);
        return positionRepository.count(specification);
    }

    /**
     * Function to convert {@link PositionCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Position> createSpecification(PositionCriteria criteria) {
        Specification<Position> specification = Specification.unrestricted();
        specification = specification.and((root, query, builder) -> {
            if (Long.class != query.getResultType()) {
                root.fetch(Position_.account, JoinType.LEFT);
                root.fetch(Position_.asset, JoinType.LEFT);
            }
            return null;
        });
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = specification.and(
                Specification.allOf(
                    Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                    buildSpecification(criteria.getId(), Position_.id),
                    buildRangeSpecification(criteria.getQuantity(), Position_.quantity),
                    buildRangeSpecification(criteria.getAverageCost(), Position_.averageCost),
                    buildRangeSpecification(criteria.getCurrentValue(), Position_.currentValue),
                    buildStringSpecification(criteria.getCurrency(), Position_.currency),
                    buildRangeSpecification(criteria.getLastSyncedAt(), Position_.lastSyncedAt),
                    buildSpecification(criteria.getAccountId(), root -> root.join(Position_.account, JoinType.LEFT).get(BrokerAccount_.id)),
                    buildSpecification(criteria.getAssetId(), root -> root.join(Position_.asset, JoinType.LEFT).get(Asset_.id))
                )
            );
        }
        return specification;
    }
}
