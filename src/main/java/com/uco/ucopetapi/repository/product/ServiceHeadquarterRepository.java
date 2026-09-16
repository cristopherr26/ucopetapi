package com.uco.ucopetapi.repository.product;

import com.uco.ucopetapi.domain.product.ServiceHeadquarterDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ServiceHeadquarterRepository extends JpaRepository<ServiceHeadquarterDomain, UUID> {

    List<ServiceHeadquarterDomain> findByService_Id(UUID serviceId);

    @Modifying
    @Query("DELETE FROM ServiceHeadquarterDomain sh WHERE sh.service.id = :serviceId")
    void deleteByService_Id(@Param("serviceId") UUID serviceId);
}