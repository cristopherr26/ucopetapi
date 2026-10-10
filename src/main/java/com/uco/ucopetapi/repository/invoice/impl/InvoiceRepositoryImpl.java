package com.uco.ucopetapi.repository.invoice.impl;

import com.uco.ucopetapi.domain.invoice.InvoiceDomain;
import com.uco.ucopetapi.dto.invoice.InvoiceDTO;
import com.uco.ucopetapi.repository.invoice.InvoiceRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class InvoiceRepositoryImpl implements InvoiceRepository {

    private final InvoiceJpaRepository jpaRepository;

    public InvoiceRepositoryImpl(InvoiceJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public InvoiceDomain save(InvoiceDomain invoiceDomain) {
        return jpaRepository.save(invoiceDomain);
    }

    @Override
    public Optional<InvoiceDomain> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public boolean existsBySaleOrderId(UUID saleOrderId) {
        return jpaRepository.existsBySaleOrderId(saleOrderId);
    }

    @Override
    public List<InvoiceDomain> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public List<InvoiceDomain> findByFilter(InvoiceDTO filter, LocalDate dateFrom, LocalDate dateTo) {
        return jpaRepository.findAll(buildSpecification(filter, dateFrom, dateTo));
    }

    @Override
    public Optional<InvoiceDomain> findLastInvoiceNumberByHeadquarter(UUID headquarterId) {
        return jpaRepository.findTopByHeadquarterIdOrderByInvoiceNumberDesc(headquarterId);
    }

    private Specification<InvoiceDomain> buildSpecification(InvoiceDTO filter, LocalDate dateFrom, LocalDate dateTo) {
        InvoiceDTO defaults = InvoiceDTO.getDefaultObject();

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            addEqualityPredicates(predicates, root, cb, filter, defaults);
            addDateRangePredicates(predicates, root, cb, dateFrom, dateTo);
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void addEqualityPredicates(List<Predicate> predicates, Root<InvoiceDomain> root, CriteriaBuilder cb,
                                        InvoiceDTO filter, InvoiceDTO defaults) {
        if (!filter.getHeadquarterId().equals(defaults.getHeadquarterId())) {
            predicates.add(cb.equal(root.get("headquarterId"), filter.getHeadquarterId()));
        }
        if (!filter.getClientID().equals(defaults.getClientID())) {
            predicates.add(cb.equal(root.get("clientID"), filter.getClientID()));
        }
        if (!filter.getPetId().equals(defaults.getPetId())) {
            predicates.add(cb.equal(root.get("petId"), filter.getPetId()));
        }
        if (!filter.getSaleOrderId().equals(defaults.getSaleOrderId())) {
            predicates.add(cb.equal(root.get("saleOrderId"), filter.getSaleOrderId()));
        }
        if (!filter.getInvoiceNumber().equals(defaults.getInvoiceNumber())) {
            predicates.add(cb.equal(root.get("invoiceNumber"), filter.getInvoiceNumber()));
        }
        if (filter.getState() != defaults.getState()) {
            predicates.add(cb.equal(root.get("state"), filter.getState()));
        }
    }

    private void addDateRangePredicates(List<Predicate> predicates, Root<InvoiceDomain> root, CriteriaBuilder cb,
                                         LocalDate dateFrom, LocalDate dateTo) {
        if (dateFrom != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("issueDate"), dateFrom.atStartOfDay()));
        }
        if (dateTo != null) {
            predicates.add(cb.lessThan(root.get("issueDate"), dateTo.plusDays(1).atStartOfDay()));
        }
    }
}
