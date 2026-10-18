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
public interface IStockRepository extends JpaRepository<StockDomain, UUID> {

    Optional<StockDomain> findByItem_IdAndHeadquarter_Id(UUID productId, UUID headquarterId);

    List<StockDomain> findByItem_Id(UUID productId);

    List<StockDomain> findByItem_IdInAndHeadquarter_Id(List<UUID> productIds, UUID headquarterId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE StockDomain s SET s.quantity = s.quantity + :quantity "
            + "WHERE s.item.id = :productId AND s.headquarter.id = :headquarterId "
            + "AND s.quantity + :quantity >= 0")
    int addQuantity(@Param("productId") UUID productId,
                    @Param("headquarterId") UUID headquarterId,
                    @Param("quantity") int quantity);

    @Modifying(clearAutomatically = true)
    @Query(value = "INSERT INTO stocks (id, product_id, headquarter_id, quantity) "
            + "VALUES (:id, :productId, :headquarterId, :quantity) "
            + "ON CONFLICT (product_id, headquarter_id) DO UPDATE "
            + "SET quantity = stocks.quantity + EXCLUDED.quantity", nativeQuery = true)
    void createOrAddQuantity(@Param("id") UUID id,
                            @Param("productId") UUID productId,
                            @Param("headquarterId") UUID headquarterId,
                            @Param("quantity") int quantity);
}