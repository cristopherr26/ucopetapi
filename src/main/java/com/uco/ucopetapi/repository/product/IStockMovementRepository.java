package com.uco.ucopetapi.repository.product;

import com.uco.ucopetapi.domain.product.StockMovementDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IStockMovementRepository extends JpaRepository<StockMovementDomain, UUID> {

    List<StockMovementDomain> findByProduct_IdOrderByOccurredAtDesc(UUID productId);

    List<StockMovementDomain> findByProduct_IdAndHeadquarterIdOrderByOccurredAtDesc(UUID productId,
                                                                                      UUID headquarterId);
}
