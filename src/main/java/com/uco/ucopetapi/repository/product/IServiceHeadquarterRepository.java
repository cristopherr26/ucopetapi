package com.uco.ucopetapi.repository.product;

import com.uco.ucopetapi.domain.product.ServiceHeadquarterDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IServiceHeadquarterRepository extends JpaRepository<ServiceHeadquarterDomain, UUID> {

    List<ServiceHeadquarterDomain> findByService_Id(UUID serviceId);
}
