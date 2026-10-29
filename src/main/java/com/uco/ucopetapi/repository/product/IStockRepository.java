package com.uco.ucopetapi.repository.product;

import com.uco.ucopetapi.domain.product.StockDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IStockRepository extends JpaRepository<StockDomain, UUID> {

    Optional<StockDomain> findByProduct_IdAndHeadquarterId(UUID productId, UUID headquarterId);

    List<StockDomain> findByProduct_Id(UUID productId);

    List<StockDomain> findByHeadquarterId(UUID headquarterId);
}
