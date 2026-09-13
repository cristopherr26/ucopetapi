package com.uco.ucopetapi.repository.provider;

import com.uco.ucopetapi.domain.provider.ProviderDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProviderJPARepository extends JpaRepository<ProviderDomain, UUID> {

    List<ProviderDomain> findByIdType(UUID idType);

    List<ProviderDomain> findByActive(boolean active);

    List<ProviderDomain> findByIdTypeAndActive(UUID idType, boolean active);
}
