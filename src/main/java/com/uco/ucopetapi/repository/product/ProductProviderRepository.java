package com.uco.ucopetapi.repository.product;

import com.uco.ucopetapi.domain.product.ProductProviderDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductProviderRepository extends JpaRepository<ProductProviderDomain, UUID> {

    List<ProductProviderDomain> findByProduct_Id(UUID productId);

    List<ProductProviderDomain> findByProvider_Id(UUID providerId);

    void deleteByProduct_Id(UUID productId);
}