package com.uco.ucopetapi.repository.product;

import com.uco.ucopetapi.domain.product.ServiceDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IServiceRepository extends JpaRepository<ServiceDomain, UUID> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);

    long countByActiveTrue();
}
