package com.uco.ucopetapi.repository.procedure;

import com.uco.ucopetapi.domain.procedure.ProcedureDomain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface IProcedureRepository extends JpaRepository<ProcedureDomain, UUID> {
    Optional<ProcedureDomain> findByCode(String code);
}
