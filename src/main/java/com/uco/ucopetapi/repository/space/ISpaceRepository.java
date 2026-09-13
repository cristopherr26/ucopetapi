package com.uco.ucopetapi.repository.space;

import com.uco.ucopetapi.domain.space.SpaceDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ISpaceRepository extends JpaRepository<SpaceDomain, UUID> {

    // Buscar un espacio específico por su código (ej: LAB-401)
    Optional<SpaceDomain> findByCode(String code);

    // Buscar todos los espacios de un tipo (ej: Consultorio)
    List<SpaceDomain> findByType(String type);

    // Buscar todos los espacios activos o inactivos (true/false)
    List<SpaceDomain> findByActive(Boolean active);
}