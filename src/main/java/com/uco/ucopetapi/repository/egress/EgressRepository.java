package com.uco.ucopetapi.repository.egress;

import com.uco.ucopetapi.domain.egress.EgressDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface EgressRepository extends JpaRepository<EgressDomain, UUID> {

    List<EgressDomain> findByProduct(String product);

    List<EgressDomain> findByDateBetween(LocalDate startDate, LocalDate endDate);

    //List<EgressDomain> findByProvider_Id(UUID providerId);

    List<EgressDomain> findByPayMethod_Id(UUID payMethodId);

}
