package com.assetcompass.portfolio.service;

import com.assetcompass.portfolio.domain.*; // for static metamodels
import com.assetcompass.portfolio.domain.Asset;
import com.assetcompass.portfolio.repository.AssetRepository;
import com.assetcompass.portfolio.service.criteria.AssetCriteria;
import com.assetcompass.portfolio.service.dto.AssetDTO;
import com.assetcompass.portfolio.service.mapper.AssetMapper;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link Asset} entities in the database.
 * The main input is a {@link AssetCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link AssetDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class AssetQueryService extends QueryService<Asset> {

    private static final Logger LOG = LoggerFactory.getLogger(AssetQueryService.class);

    private final AssetRepository assetRepository;

    private final AssetMapper assetMapper;

    public AssetQueryService(AssetRepository assetRepository, AssetMapper assetMapper) {
        this.assetRepository = assetRepository;
        this.assetMapper = assetMapper;
    }

    /**
     * Return a {@link List} of {@link AssetDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<AssetDTO> findByCriteria(AssetCriteria criteria) {
        LOG.debug("find by criteria : {}", criteria);
        final Specification<Asset> specification = createSpecification(criteria);
        return assetMapper.toDto(assetRepository.findAll(specification));
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(AssetCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Asset> specification = createSpecification(criteria);
        return assetRepository.count(specification);
    }

    /**
     * Function to convert {@link AssetCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Asset> createSpecification(AssetCriteria criteria) {
        Specification<Asset> specification = Specification.unrestricted();
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = specification.and(
                Specification.allOf(
                    Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                    buildSpecification(criteria.getId(), Asset_.id),
                    buildStringSpecification(criteria.getTicket(), Asset_.ticket),
                    buildStringSpecification(criteria.getCategory(), Asset_.category),
                    buildStringSpecification(criteria.getCountry(), Asset_.country),
                    buildStringSpecification(criteria.getDescription(), Asset_.description)
                )
            );
        }
        return specification;
    }
}
