package com.uco.ucopetapi.repository.payMethod;

import com.uco.ucopetapi.domain.payMethod.PayMethodDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PayMethodRepository extends JpaRepository<PayMethodDomain, UUID> {

    Optional<PayMethodDomain> findByName(String name);
}
