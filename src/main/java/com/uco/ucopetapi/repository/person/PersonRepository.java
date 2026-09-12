package com.uco.ucopetapi.repository.person;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.uco.ucopetapi.domain.person.PersonDomain;
import com.uco.ucopetapi.dto.person.DocumentType;

@Repository
public interface PersonRepository extends JpaRepository<PersonDomain, UUID> {
    Optional<PersonDomain> findByEmail(String email);

    long countByAdminTrueAndActiveTrue();

    Optional<PersonDomain> findByDocumentTypeAndDocumentNumber(DocumentType documentType, String documentNumber);

}
