package com.uco.ucopetapi.repository.product;

import com.uco.ucopetapi.domain.product.enums.ProductCategory;
import com.uco.ucopetapi.domain.product.ProductDomain;
import com.uco.ucopetapi.domain.product.enums.TaxCategory;
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
        WHERE (:category IS NULL OR p.category = :category)
          AND (:active IS NULL OR p.active = :active)
          AND (:sellable IS NULL OR p.sellable = :sellable)
          AND (:taxCategory IS NULL OR p.taxCategory = :taxCategory)
        """)
    List<ProductDomain> findByFilter(
            @Param("category") ProductCategory category,
            @Param("active") Boolean active,
            @Param("sellable") Boolean sellable,
            @Param("taxCategory") TaxCategory taxCategory
    );
}