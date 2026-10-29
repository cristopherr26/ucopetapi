package com.uco.ucopetapi.repository.product;

import com.uco.ucopetapi.domain.product.HeadquarterReferenceDomain;
import org.springframework.data.repository.Repository;

import java.util.UUID;

public interface IHeadquarterReferenceRepository extends Repository<HeadquarterReferenceDomain, UUID> {

    boolean existsById(UUID id);

    boolean existsByIdAndActiveTrue(UUID id);
}
