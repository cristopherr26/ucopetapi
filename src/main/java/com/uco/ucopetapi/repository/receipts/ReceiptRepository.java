package com.uco.ucopetapi.repository.receipts;

import com.uco.ucopetapi.domain.receipts.ReceiptDomain;
import com.uco.ucopetapi.dto.receipts.ReceiptStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReceiptRepository extends JpaRepository<ReceiptDomain, UUID> {

    List<ReceiptDomain> findByTutorId(UUID tutorId);

    List<ReceiptDomain> findByState(ReceiptStatus state);

    List<ReceiptDomain> findByTutorIdAndState(UUID tutorId, ReceiptStatus state);

    List<ReceiptDomain> findByDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    boolean existsByReceiptNumber(String receiptNumber);
}
