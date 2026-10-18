package com.uco.ucopetapi.repository.product;

import com.uco.ucopetapi.domain.product.CatalogItemHeadquarterDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ICatalogItemHeadquarterRepository extends JpaRepository<CatalogItemHeadquarterDomain, UUID> {

    List<CatalogItemHeadquarterDomain> findByItem_Id(UUID serviceId);

    List<CatalogItemHeadquarterDomain> findByItem_IdIn(List<UUID> serviceIds);

    @Modifying
    @Query("DELETE FROM CatalogItemHeadquarterDomain sh WHERE sh.item.id = :serviceId")
    void deleteByItem_Id(@Param("serviceId") UUID serviceId);
}