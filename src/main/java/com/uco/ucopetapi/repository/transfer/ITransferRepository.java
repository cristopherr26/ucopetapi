package com.uco.ucopetapi.repository.transfer;

import com.uco.ucopetapi.domain.transfer.TransferDomain;
import com.uco.ucopetapi.dto.transfers.TransferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ITransferRepository extends JpaRepository<TransferDomain, UUID> {

    @Query("""
            SELECT t FROM TransferDomain t
            WHERE (:status IS NULL OR t.status = :status)
              AND (:originId IS NULL OR t.originHeadquarter.id = :originId)
              AND (:destinationId IS NULL OR t.destinationHeadquarter.id = :destinationId)
            """)
    List<TransferDomain> findByFilter(@Param("status") TransferStatus status,
                                      @Param("originId") UUID originId,
                                      @Param("destinationId") UUID destinationId);
}