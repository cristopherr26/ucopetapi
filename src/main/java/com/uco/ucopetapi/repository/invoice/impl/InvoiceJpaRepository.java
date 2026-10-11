package com.uco.ucopetapi.repository.invoice.impl;

import com.uco.ucopetapi.domain.invoice.InvoiceDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface InvoiceJpaRepository extends JpaRepository<InvoiceDomain, UUID>,
        JpaSpecificationExecutor<InvoiceDomain> {

    boolean existsBySaleOrderId(UUID saleOrderId);

    Optional<InvoiceDomain> findTopByHeadquarterIdOrderByInvoiceNumberDesc(UUID headquarterId);
}
