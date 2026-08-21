package com.assetcompass.portfolio.service;

import com.assetcompass.portfolio.domain.*; // for static metamodels
import com.assetcompass.portfolio.domain.Broker;
import com.assetcompass.portfolio.repository.BrokerRepository;
import com.assetcompass.portfolio.service.criteria.BrokerCriteria;
import com.assetcompass.portfolio.service.dto.BrokerDTO;
import com.assetcompass.portfolio.service.mapper.BrokerMapper;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link Broker} entities in the database.
 * The main input is a {@link BrokerCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link BrokerDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class BrokerQueryService extends QueryService<Broker> {

    private static final Logger LOG = LoggerFactory.getLogger(BrokerQueryService.class);

    private final BrokerRepository brokerRepository;

    private final BrokerMapper brokerMapper;

    public BrokerQueryService(BrokerRepository brokerRepository, BrokerMapper brokerMapper) {
        this.brokerRepository = brokerRepository;
        this.brokerMapper = brokerMapper;
    }

    /**
     * Return a {@link List} of {@link BrokerDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<BrokerDTO> findByCriteria(BrokerCriteria criteria) {
        LOG.debug("find by criteria : {}", criteria);
        final Specification<Broker> specification = createSpecification(criteria);
        return brokerMapper.toDto(brokerRepository.findAll(specification));
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(BrokerCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Broker> specification = createSpecification(criteria);
        return brokerRepository.count(specification);
    }

    /**
     * Function to convert {@link BrokerCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Broker> createSpecification(BrokerCriteria criteria) {
        Specification<Broker> specification = Specification.unrestricted();
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = specification.and(
                Specification.allOf(
                    Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                    buildSpecification(criteria.getId(), Broker_.id),
                    buildStringSpecification(criteria.getName(), Broker_.name)
                )
            );
        }
        return specification;
    }
}
