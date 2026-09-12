package com.uco.ucopetapi.repository.product;

import com.uco.ucopetapi.domain.product.ProductDomain;
import com.uco.ucopetapi.domain.product.ProductStatus;
import com.uco.ucopetapi.domain.product.ProductType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<ProductDomain, UUID> {

    @Query("""
        SELECT p FROM ProductDomain p
        WHERE (:type IS NULL OR p.type = :type)
          AND (:category IS NULL OR p.category = :category)
          AND (:status IS NULL OR p.status = :status)
        """)
    List<ProductDomain> findByFilter(
            @Param("type") ProductType type,
            @Param("category") String category,
            @Param("status") ProductStatus status
    );
}