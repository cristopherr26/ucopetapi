package com.uco.ucopetapi.repository.invoice;

import com.uco.ucopetapi.domain.invoice.InvoiceDomain;
import com.uco.ucopetapi.dto.invoice.InvoiceDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository {

    InvoiceDomain save(InvoiceDomain invoiceDomain);

    Optional<InvoiceDomain> findById(UUID id);

    boolean existsById(UUID id);

    boolean existsBySaleOrderId(UUID saleOrderId);

    List<InvoiceDomain> findAll();

    List<InvoiceDomain> findByFilter(InvoiceDTO filter, LocalDate dateFrom, LocalDate dateTo);

    Optional<InvoiceDomain> findLastInvoiceNumberByHeadquarter(UUID headquarterId);
}
