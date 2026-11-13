package com.uco.ucopetapi.repository.sale.sale.impl;

import com.uco.ucopetapi.domain.sale.enums.SaleOrderState;
import com.uco.ucopetapi.domain.sale.sale.SaleOrderDomain;
import com.uco.ucopetapi.repository.sale.sale.SaleOrderJpaRepository;
import com.uco.ucopetapi.repository.sale.sale.SaleOrderRepository;
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

    private static final UUID DEFAULT_UUID =
            UUID.fromString("00000000-0000-0000-0000-000000000000");

    private static final String DEFAULT_TEXT = "";

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
    public List<SaleOrderDomain> findByFilter(
            SaleOrderDomain filter,
            LocalDate dateFrom,
            LocalDate dateTo) {

        return jpaRepository.findAll(
                buildSpecification(filter, dateFrom, dateTo)
        );
    }

    @Override
    public Optional<SaleOrderDomain> findLastOrderNumberByHeadquarter(UUID headquarterId) {
        return jpaRepository.findTopByHeadquarterIdOrderByOrderNumberDesc(headquarterId);
    }

    private Specification<SaleOrderDomain> buildSpecification(
            SaleOrderDomain filter,
            LocalDate dateFrom,
            LocalDate dateTo) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (filter.getHeadquarterId() != null
                    && !DEFAULT_UUID.equals(filter.getHeadquarterId())) {
                predicates.add(
                        cb.equal(root.get("headquarterId"), filter.getHeadquarterId())
                );
            }

            if (filter.getClientID() != null
                    && !DEFAULT_UUID.equals(filter.getClientID())) {
                predicates.add(
                        cb.equal(root.get("clientID"), filter.getClientID())
                );
            }

            if (filter.getPetId() != null
                    && !DEFAULT_UUID.equals(filter.getPetId())) {
                predicates.add(
                        cb.equal(root.get("petId"), filter.getPetId())
                );
            }

            if (filter.getHealthPlanId() != null
                    && !DEFAULT_UUID.equals(filter.getHealthPlanId())) {
                predicates.add(
                        cb.equal(root.get("healthPlanId"), filter.getHealthPlanId())
                );
            }

            if (filter.getOrderNumber() != null
                    && !DEFAULT_TEXT.equals(filter.getOrderNumber())) {
                predicates.add(
                        cb.equal(root.get("orderNumber"), filter.getOrderNumber())
                );
            }

            if (filter.getState() != null
                    && filter.getState() != SaleOrderState.SIN_ESTADO) {
                predicates.add(
                        cb.equal(root.get("state"), filter.getState())
                );
            }

            if (dateFrom != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(
                                root.get("saleDate"),
                                dateFrom.atStartOfDay()
                        )
                );
            }

            if (dateTo != null) {
                predicates.add(
                        cb.lessThan(
                                root.get("saleDate"),
                                dateTo.plusDays(1).atStartOfDay()
                        )
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
