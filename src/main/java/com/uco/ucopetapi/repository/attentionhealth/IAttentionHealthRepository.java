package com.uco.ucopetapi.repository.attentionhealth;

import com.uco.ucopetapi.domain.attentionhealth.AttentionHealthDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence contract for the new attention functionality.
 * Related products and procedures are stored as identifier collections owned
 * by the attention aggregate; they do not require separate repositories here.
 */
public interface IAttentionHealthRepository extends JpaRepository<AttentionHealthDomain, Long> {

    @Query(value = "SELECT nextval(pg_get_serial_sequence('attention_health', 'attention_id'))", nativeQuery = true)
    Long nextAttentionId();

    Optional<AttentionHealthDomain> findByAppointmentId(UUID appointmentId);

    List<AttentionHealthDomain> findByPetIdOrderByAttentionDateDesc(UUID petId);

    boolean existsByAppointmentId(UUID appointmentId);
}


