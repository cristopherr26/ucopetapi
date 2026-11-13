package com.uco.ucopetapi.repository.purchases;

import com.uco.ucopetapi.domain.purchases.Purchase;
import com.uco.ucopetapi.domain.purchases.PurchaseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PurchaseRepository extends JpaRepository<Purchase, UUID> {

    Page<Purchase> findByHeadquarterId(UUID headquarterId, Pageable pageable);

    Page<Purchase> findByHeadquarterIdAndStatus(UUID headquarterId, PurchaseStatus status, Pageable pageable);

    Page<Purchase> findByHeadquarterIdAndSupplierId(UUID headquarterId, UUID supplierId, Pageable pageable);

    Page<Purchase> findByHeadquarterIdAndStatusAndSupplierId(UUID headquarterId, PurchaseStatus status, UUID supplierId, Pageable pageable);

    boolean existsByPurchaseNumber(String purchaseNumber);

    Optional<Purchase> findByPurchaseNumber(String purchaseNumber);
}
