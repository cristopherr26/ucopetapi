package com.uco.ucopetapi.service.appointment;

import com.uco.ucopetapi.domain.doctor.DoctorDomain;
import com.uco.ucopetapi.dto.appointment.AppointmentDTO;
import com.uco.ucopetapi.dto.person.Role;
import com.uco.ucopetapi.repository.appointment.IAppointmentRepository;
import com.uco.ucopetapi.repository.doctor.IDoctorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Regla de acceso del modulo de citas: un ADMIN ve y modifica todas las citas; un usuario que solo
 * es DOCTOR unicamente las suyas. La identidad sale del token (el "sub" es el id de la persona) y se
 * convierte en los ids de medico de esa persona; el cliente nunca decide de quien son las citas.
 * Se usa desde el controlador, asi los flujos internos (atencion) no quedan afectados.
 */
@Component
public class AppointmentAccessPolicy {

    private static final String NOT_OWN_MESSAGE = "Solo puedes ver y modificar tus propias citas";

    private final IDoctorRepository doctorRepository;
    private final IAppointmentRepository appointmentRepository;

    public AppointmentAccessPolicy(IDoctorRepository doctorRepository,
                                   IAppointmentRepository appointmentRepository) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
    }

    /** Deja solo las citas que quien consulta puede ver. */
    public List<AppointmentDTO> visibleTo(List<AppointmentDTO> appointments) {
        Optional<Set<UUID>> allowed = restrictedDoctorIds();
        if (allowed.isEmpty()) {
            return appointments;
        }
        return appointments.stream()
                .filter(appointment -> allowed.get().contains(appointment.getDoctorId()))
                .toList();
    }

    /** Rechaza (403) si quien consulta no puede actuar sobre citas del medico indicado. */
    public void requireOwnDoctor(UUID doctorId) {
        Optional<Set<UUID>> allowed = restrictedDoctorIds();
        if (allowed.isPresent() && (doctorId == null || !allowed.get().contains(doctorId))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, NOT_OWN_MESSAGE);
        }
    }

    /** Rechaza (403) a quien no sea administrador. */
    public void requireAdmin(String message) {
        if (restrictedDoctorIds().isPresent()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, message);
        }
    }

    /** El medico de quien consulta (por el token), o vacio si no es medico. */
    public Optional<DoctorDomain> ownDoctor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        try {
            UUID personId = UUID.fromString(authentication.getName());
            return doctorRepository.findByIdPerson(personId).stream().findFirst();
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    /** Rechaza (403) si la cita guardada es de otro medico. Si no existe no hace nada: responde el 404 normal. */
    public void requireOwnAppointment(UUID appointmentId) {
        Optional<Set<UUID>> allowed = restrictedDoctorIds();
        if (allowed.isEmpty()) {
            return;
        }
        appointmentRepository.findById(appointmentId).ifPresent(appointment -> {
            if (!allowed.get().contains(appointment.getDoctorId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, NOT_OWN_MESSAGE);
            }
        });
    }

    /**
     * Ids de medico a los que queda limitado quien consulta, o vacio si no tiene limite (ADMIN).
     * Si no hay una sesion valida o el rol no es DOCTOR, devuelve un conjunto vacio: no ve nada.
     */
    private Optional<Set<UUID>> restrictedDoctorIds() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.of(Set.of());
        }
        if (hasAuthority(authentication, Role.ADMIN.authority())) {
            return Optional.empty();
        }
        if (!hasAuthority(authentication, Role.DOCTOR.authority())) {
            return Optional.of(Set.of());
        }
        try {
            UUID personId = UUID.fromString(authentication.getName());
            Set<UUID> doctorIds = doctorRepository.findByIdPerson(personId).stream()
                    .map(DoctorDomain::getId)
                    .collect(Collectors.toSet());
            return Optional.of(doctorIds);
        } catch (IllegalArgumentException ex) {
            return Optional.of(Set.of());
        }
    }

    private boolean hasAuthority(Authentication authentication, String authority) {
        for (GrantedAuthority granted : authentication.getAuthorities()) {
            if (authority.equals(granted.getAuthority())) {
                return true;
            }
        }
        return false;
    }
}
