package com.uco.ucopetapi.repository.product;

import com.uco.ucopetapi.domain.product.ServiceProviderDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ServiceProviderRepository extends JpaRepository<ServiceProviderDomain, UUID> {

    List<ServiceProviderDomain> findByService_Id(UUID serviceId);

    List<ServiceProviderDomain> findByProvider_Id(UUID providerId);

    @Modifying
    @Query("DELETE FROM ServiceProviderDomain s WHERE s.service.id = :serviceId")
    void deleteByService_Id(@Param("serviceId") UUID serviceId);
}