package com.uco.ucopetapi.security;

import com.uco.ucopetapi.domain.doctor.DoctorDomain;
import com.uco.ucopetapi.dto.person.Role;
import com.uco.ucopetapi.repository.doctor.IDoctorRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class HeadquarterScope {

    static final String OTHER_HEADQUARTER = "Solo puedes consultar la sede donde trabajas";

    private final IDoctorRepository doctorRepository;

    public HeadquarterScope(final IDoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    public void requireReadable(final UUID headquarterId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || isAdmin(authentication)) {
            return;
        }
        if (headquarterId == null || !ownHeadquarters(authentication).contains(headquarterId)) {
            throw new HeadquarterForbiddenException(OTHER_HEADQUARTER);
        }
    }

    private static boolean isAdmin(final Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> Role.ADMIN.authority().equals(authority.getAuthority()));
    }

    private Set<UUID> ownHeadquarters(final Authentication authentication) {
        UUID personId;
        try {
            personId = UUID.fromString(authentication.getName());
        } catch (IllegalArgumentException exception) {
            return Set.of();
        }
        return doctorRepository.findByIdPerson(personId).stream()
                .map(DoctorDomain::getHeadquarterId)
                .collect(Collectors.toSet());
    }
}
