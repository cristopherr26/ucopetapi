package com.uco.ucopetapi.services.purchases;

import com.uco.ucopetapi.domain.purchases.PurchaseStatus;
import com.uco.ucopetapi.dto.purchases.LinkExpenseRequestDTO;
import com.uco.ucopetapi.dto.purchases.PurchaseRequestDTO;
import com.uco.ucopetapi.dto.purchases.PurchaseResponseDTO;
import com.uco.ucopetapi.dto.purchases.PurchaseResponseDTO.Item;

import java.util.List;
import java.util.UUID;

public interface PurchaseService {

    PurchaseResponseDTO createPurchase(PurchaseRequestDTO request);

    PurchaseResponseDTO getPurchaseById(UUID id);

    List<PurchaseResponseDTO> listPurchases(UUID headquarterId, PurchaseStatus status, UUID supplierId);

    List<Item> getPurchaseItems(UUID id);

    PurchaseResponseDTO receivePurchase(UUID id);

    PurchaseResponseDTO cancelPurchase(UUID id);

    PurchaseResponseDTO linkExpense(UUID id, LinkExpenseRequestDTO request);
}
