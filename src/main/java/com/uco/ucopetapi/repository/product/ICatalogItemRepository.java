package com.uco.ucopetapi.repository.product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.uco.ucopetapi.domain.product.CatalogItemDomain;
import com.uco.ucopetapi.domain.product.enums.CatalogCategory;
import com.uco.ucopetapi.domain.product.enums.CatalogItemType;
import com.uco.ucopetapi.domain.product.enums.TaxCategory;

@Repository
public interface ICatalogItemRepository extends JpaRepository<CatalogItemDomain, UUID> {

    @Query("""
            SELECT i FROM CatalogItemDomain i
            WHERE (:type IS NULL OR i.type = :type)
              AND (:category IS NULL OR i.category = :category)
              AND (:active IS NULL OR i.active = :active)
              AND (:purchasable IS NULL OR i.purchasable = :purchasable)
              AND (:sellable IS NULL OR i.sellable = :sellable)
              AND (:taxCategory IS NULL OR i.taxCategory = :taxCategory)
              AND (:headquarterId IS NULL OR EXISTS (
                    SELECT 1 FROM StockDomain s
                    WHERE s.item = i AND s.headquarter.id = :headquarterId
                  ) OR EXISTS (
                    SELECT 1 FROM CatalogItemHeadquarterDomain h
                    WHERE h.item = i AND h.headquarter.id = :headquarterId
                  ))
            """)
    List<CatalogItemDomain> findByFilter(
            @Param("type") CatalogItemType type,
            @Param("category") CatalogCategory category,
            @Param("active") Boolean active,
            @Param("purchasable") Boolean purchasable,
            @Param("sellable") Boolean sellable,
            @Param("taxCategory") TaxCategory taxCategory,
            @Param("headquarterId") UUID headquarterId);

    Optional<CatalogItemDomain> findByIdAndType(UUID id, CatalogItemType type);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);

    long countByTypeAndActiveTrue(CatalogItemType type);
}
