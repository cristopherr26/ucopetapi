package com.uco.ucopetapi.repository.specialtieDoctor;

import com.uco.ucopetapi.domain.specialtieDoctor.SpecialtieDoctorDomain;
import com.uco.ucopetapi.domain.specialtieDoctor.VerificationStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Consultas dinámicas sobre specialtie_doctors. Las filas antiguas pueden tener status o is_active
 * en null: se tratan con coalesce como PENDING y como activas, respectivamente.
 */
public final class SpecialtieDoctorSpecifications {

    private SpecialtieDoctorSpecifications() {
    }

    /**
     * @param headquarterId obligatorio
     * @param idDoctor      opcional
     * @param idSpecialtie  opcional
     * @param status        opcional; un status null en BD cuenta como PENDING
     * @param onlyActive    si es true, solo relaciones activas (is_active null cuenta como activa)
     */
    public static Specification<SpecialtieDoctorDomain> filter(UUID headquarterId, UUID idDoctor, UUID idSpecialtie,
                                                               VerificationStatus status, boolean onlyActive) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("headquarterId"), headquarterId));

            if (idDoctor != null) {
                predicates.add(cb.equal(root.get("doctor").get("id"), idDoctor));
            }
            if (idSpecialtie != null) {
                predicates.add(cb.equal(root.get("idSpecialtie"), idSpecialtie));
            }
            if (status != null) {
                predicates.add(cb.equal(
                        cb.coalesce(root.<VerificationStatus>get("status"), VerificationStatus.PENDING),
                        status));
            }
            if (onlyActive) {
                predicates.add(cb.isTrue(cb.coalesce(root.<Boolean>get("isActive"), Boolean.TRUE)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
