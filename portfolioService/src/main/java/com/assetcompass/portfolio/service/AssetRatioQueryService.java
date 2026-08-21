package com.assetcompass.portfolio.service;

import com.assetcompass.portfolio.domain.*; // for static metamodels
import com.assetcompass.portfolio.domain.AssetRatio;
import com.assetcompass.portfolio.repository.AssetRatioRepository;
import com.assetcompass.portfolio.service.criteria.AssetRatioCriteria;
import com.assetcompass.portfolio.service.dto.AssetRatioDTO;
import com.assetcompass.portfolio.service.mapper.AssetRatioMapper;
import jakarta.persistence.criteria.JoinType;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link AssetRatio} entities in the database.
 * The main input is a {@link AssetRatioCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link AssetRatioDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class AssetRatioQueryService extends QueryService<AssetRatio> {

    private static final Logger LOG = LoggerFactory.getLogger(AssetRatioQueryService.class);

    private final AssetRatioRepository assetRatioRepository;

    private final AssetRatioMapper assetRatioMapper;

    public AssetRatioQueryService(AssetRatioRepository assetRatioRepository, AssetRatioMapper assetRatioMapper) {
        this.assetRatioRepository = assetRatioRepository;
        this.assetRatioMapper = assetRatioMapper;
    }

    /**
     * Return a {@link List} of {@link AssetRatioDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<AssetRatioDTO> findByCriteria(AssetRatioCriteria criteria) {
        LOG.debug("find by criteria : {}", criteria);
        final Specification<AssetRatio> specification = createSpecification(criteria);
        return assetRatioMapper.toDto(assetRatioRepository.findAll(specification));
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(AssetRatioCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<AssetRatio> specification = createSpecification(criteria);
        return assetRatioRepository.count(specification);
    }

    /**
     * Function to convert {@link AssetRatioCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<AssetRatio> createSpecification(AssetRatioCriteria criteria) {
        Specification<AssetRatio> specification = Specification.unrestricted();
        specification = specification.and((root, query, builder) -> {
            if (Long.class != query.getResultType()) {
                root.fetch(AssetRatio_.asset, JoinType.LEFT);
            }
            return null;
        });
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = specification.and(
                Specification.allOf(
                    Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                    buildSpecification(criteria.getId(), AssetRatio_.id),
                    buildStringSpecification(criteria.getRatio(), AssetRatio_.ratio),
                    buildRangeSpecification(criteria.getEffectiveFrom(), AssetRatio_.effectiveFrom),
                    buildSpecification(criteria.getAssetId(), root -> root.join(AssetRatio_.asset, JoinType.LEFT).get(Asset_.id))
                )
            );
        }
        return specification;
    }
}
