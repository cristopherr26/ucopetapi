// repository/product/StockRepository.java
package com.uco.ucopetapi.repository.product;

import com.uco.ucopetapi.domain.product.StockDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StockRepository extends JpaRepository<StockDomain, UUID> {

    Optional<StockDomain> findByProduct_IdAndHeadquarter_Id(UUID productId, UUID headquarterId);
}