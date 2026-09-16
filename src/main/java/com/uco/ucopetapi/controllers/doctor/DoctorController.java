package com.uco.ucopetapi.controllers.doctor;

import com.uco.ucopetapi.domain.doctor.DoctorDomain;
import com.uco.ucopetapi.dto.doctor.DoctorDTO;
import com.uco.ucopetapi.exception.BusinessException;
import com.uco.ucopetapi.service.doctor.DoctorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

// Controller REST para el modulo de Doctor.
// Expone los endpoints en /api/v1/doctor y delega toda la logica de negocio
@RestController
@RequestMapping("api/v1/doctor")
public class DoctorController {

    private static final String MESSAGE_KEY = "message";

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    private DoctorDTO toDTO(DoctorDomain doctor) {
        return new DoctorDTO(doctor.getId(), doctor.getIdPerson(), doctor.getLicenseNumber());
    }

    private DoctorDomain toDomain(DoctorDTO doctor) {
        return new DoctorDomain(doctor.getId(), doctor.getPerson(), doctor.getLicenseNumber());
    }

    // Devuelve todos los doctores registrados.
    @GetMapping
    public ResponseEntity<List<DoctorDTO>> findAllDoctor() {
        List<DoctorDTO> doctors = doctorService.findAll().stream()
                .map(this::toDTO)
                .toList();

        return ResponseEntity.ok(doctors);
    }

    // Busca un doctor puntual por su id.
    @GetMapping("/{id}")
    public ResponseEntity<Object> findDoctorById(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(toDTO(doctorService.findById(id)));
        } catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(MESSAGE_KEY, e.getMessage()));
        }
    }

    // Filtra doctores por numero de licencia (busqueda parcial, sin distinguir mayusculas/minusculas). Si no se manda el parametro, devuelve todos.
    @GetMapping("/filter")
    public ResponseEntity<List<DoctorDTO>> findDoctorByFilter(@RequestParam(required = false) String licenseNumber) {
        List<DoctorDTO> doctors = doctorService.findByFilter(licenseNumber).stream()
                .map(this::toDTO)
                .toList();

        return ResponseEntity.ok(doctors);
    }

    // Crea un doctor nuevo. El id que venga en el body se ignora.
    // Las validaciones (obligatoriedad, longitud y formato del licenseNumber) se hacen
    // en DoctorService.validateFields(...), que lanza BusinessException con el mensaje
    // de negocio correspondiente si algo esta mal.
    @PostMapping
    public ResponseEntity<Object> createNewDoctor(@RequestBody DoctorDTO doctor) {
        try {
            DoctorDomain createdDoctor = doctorService.createNewDoctor(toDomain(doctor));
            return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(createdDoctor));
        } catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(MESSAGE_KEY, e.getMessage()));
        }
    }

    // Actualiza los datos de un doctor existente (licencia e idPerson). Si el id no existe, responde 404 con mensaje claro (mismo manejo que arriba).
    @PutMapping
    public ResponseEntity<Object> updateDoctor(
            @RequestParam(required = true) UUID id,
            @RequestBody DoctorDTO doctor) {

        try {
            DoctorDomain updatedDoctor = doctorService.updateDoctor(id, toDomain(doctor));
            return ResponseEntity.ok(toDTO(updatedDoctor));
        } catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(MESSAGE_KEY, e.getMessage()));
        }
    }

    // Desactiva un doctor. En realidad delega en PersonService.delete(...), que hace una baja logica sobre la Person asociada (no borra el registro).
    @PutMapping("/deactivate")
    public ResponseEntity<Object> deactivateDoctor(@RequestParam(required = true) UUID id) {
        try {
            doctorService.deactivateDoctor(id);
            return ResponseEntity.noContent().build();
        } catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(MESSAGE_KEY, e.getMessage()));
        }
    }

}