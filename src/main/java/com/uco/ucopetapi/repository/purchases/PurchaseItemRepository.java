package com.uco.ucopetapi.repository.purchases;

import com.uco.ucopetapi.domain.purchases.PurchaseItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PurchaseItemRepository extends JpaRepository<PurchaseItem, UUID> {

    List<PurchaseItem> findByPurchaseId(UUID purchaseId);
}
