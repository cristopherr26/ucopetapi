package com.uco.ucopetapi.repository.sale.impl;

import com.uco.ucopetapi.domain.sale.SaleOrderDomain;
import com.uco.ucopetapi.dto.sale.SaleOrderDTO;
import com.uco.ucopetapi.repository.sale.SaleOrderRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class SaleOrderRepositoryImpl implements SaleOrderRepository {

    private final SaleOrderJpaRepository jpaRepository;

    public SaleOrderRepositoryImpl(SaleOrderJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public SaleOrderDomain save(SaleOrderDomain saleOrderDomain) {
        return jpaRepository.save(saleOrderDomain);
    }

    @Override
    public Optional<SaleOrderDomain> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<SaleOrderDomain> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public List<SaleOrderDomain> findByFilter(SaleOrderDTO filter, LocalDate dateFrom, LocalDate dateTo) {
        return jpaRepository.findAll(buildSpecification(filter, dateFrom, dateTo));
    }

    @Override
    public Optional<SaleOrderDomain> findLastOrderNumberByHeadquarter(UUID headquarterId) {
        return jpaRepository.findTopByHeadquarterIdOrderByOrderNumberDesc(headquarterId);
    }

    private Specification<SaleOrderDomain> buildSpecification(SaleOrderDTO filter, LocalDate dateFrom, LocalDate dateTo) {
        SaleOrderDTO defaults = SaleOrderDTO.getDefaultObject();

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (!filter.getHeadquarterId().equals(defaults.getHeadquarterId())) {
                predicates.add(cb.equal(root.get("headquarterId"), filter.getHeadquarterId()));
            }
            if (!filter.getClientID().equals(defaults.getClientID())) {
                predicates.add(cb.equal(root.get("clientID"), filter.getClientID()));
            }
            if (!filter.getPetId().equals(defaults.getPetId())) {
                predicates.add(cb.equal(root.get("petId"), filter.getPetId()));
            }
            if (!filter.getHealthPlanId().equals(defaults.getHealthPlanId())) {
                predicates.add(cb.equal(root.get("healthPlanId"), filter.getHealthPlanId()));
            }
            if (!filter.getOrderNumber().equals(defaults.getOrderNumber())) {
                predicates.add(cb.equal(root.get("orderNumber"), filter.getOrderNumber()));
            }
            if (filter.getState() != defaults.getState()) {
                predicates.add(cb.equal(root.get("state"), filter.getState()));
            }
            if (dateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("saleDate"), dateFrom.atStartOfDay()));
            }
            if (dateTo != null) {
                predicates.add(cb.lessThan(root.get("saleDate"), dateTo.plusDays(1).atStartOfDay()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
