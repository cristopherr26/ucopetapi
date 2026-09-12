package com.uco.ucopetapi.repository.purchases;

import com.uco.ucopetapi.domain.purchases.Purchase;
import com.uco.ucopetapi.domain.purchases.PurchaseStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PurchaseRepository extends JpaRepository<Purchase, UUID> {

    List<Purchase> findByStatus(PurchaseStatus status);

    List<Purchase> findBySupplierId(UUID supplierId);

    List<Purchase> findByStatusAndSupplierId(PurchaseStatus status, UUID supplierId);

    List<Purchase> findByHeadquarterId(UUID headquarterId);

    List<Purchase> findByHeadquarterIdAndStatus(UUID headquarterId, PurchaseStatus status);

    List<Purchase> findByHeadquarterIdAndSupplierId(UUID headquarterId, UUID supplierId);

    List<Purchase> findByHeadquarterIdAndStatusAndSupplierId(UUID headquarterId, PurchaseStatus status, UUID supplierId);
}
