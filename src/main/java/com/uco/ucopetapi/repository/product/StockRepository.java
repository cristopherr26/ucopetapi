package com.uco.ucopetapi.repository.product;

import com.uco.ucopetapi.domain.product.StockDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StockRepository extends JpaRepository<StockDomain, UUID> {
    @Modifying(clearAutomatically = true)
    @Query("""
    UPDATE StockDomain s
    SET s.quantity = s.quantity + :delta
    WHERE s.product.id = :productId
      AND s.headquarter.id = :headquarterId
      AND s.quantity + :delta >= 0
    """)
    int applyAdjustment(@Param("productId") UUID productId,
                        @Param("headquarterId") UUID headquarterId,
                        @Param("delta") Integer delta);

    Optional<StockDomain> findByProduct_IdAndHeadquarter_Id(UUID productId, UUID headquarterId);

    List<StockDomain> findByProduct_Id(UUID productId);
}