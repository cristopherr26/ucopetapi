package com.uco.ucopetapi.repository.product;

import com.uco.ucopetapi.domain.product.enums.ServiceCategory;
import com.uco.ucopetapi.domain.product.ServiceDomain;
import com.uco.ucopetapi.domain.product.enums.TaxCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceDomain, UUID> {

    @Query("""
        SELECT s FROM ServiceDomain s
        WHERE (:category IS NULL OR s.category = :category)
          AND (:active IS NULL OR s.active = :active)
          AND (:purchasable IS NULL OR s.purchasable = :purchasable)
          AND (:sellable IS NULL OR s.sellable = :sellable)
          AND (:taxCategory IS NULL OR s.taxCategory = :taxCategory)
          AND (:headquarterId IS NULL OR EXISTS (
                SELECT 1 FROM ServiceHeadquarterDomain sh
                WHERE sh.service = s AND sh.headquarter.id = :headquarterId
              ))
        """)
    List<ServiceDomain> findByFilter(
            @Param("category") ServiceCategory category,
            @Param("active") Boolean active,
            @Param("purchasable") Boolean purchasable,
            @Param("sellable") Boolean sellable,
            @Param("taxCategory") TaxCategory taxCategory,
            @Param("headquarterId") UUID headquarterId
    );
    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);

    @Query("""
        SELECT COUNT(s)
        FROM ServiceDomain s
        WHERE s.active = true
        """)
    long countActiveServices();
}