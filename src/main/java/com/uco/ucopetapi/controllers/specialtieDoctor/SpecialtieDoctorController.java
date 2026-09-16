package com.uco.ucopetapi.controllers.specialtieDoctor;

import com.uco.ucopetapi.domain.doctor.DoctorDomain;
import com.uco.ucopetapi.domain.specialtieDoctor.SpecialtieDoctorDomain;
import com.uco.ucopetapi.dto.specialtieDoctor.SpecialtieDoctorDTO;
import com.uco.ucopetapi.exception.BusinessException;
import com.uco.ucopetapi.service.doctor.DoctorService;
import com.uco.ucopetapi.service.specialtieDoctor.SpecialtieDoctorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

// Un doctor puede tener varias especialidades;
@RestController
@RequestMapping("api/v1/specialtiedoctor")
public class SpecialtieDoctorController {

    private static final String MESSAGE_KEY = "message";


    private final SpecialtieDoctorService specialtieDoctorService;
    private final DoctorService doctorService;

    public SpecialtieDoctorController(SpecialtieDoctorService specialtieDoctorService, DoctorService doctorService) {
        this.specialtieDoctorService = specialtieDoctorService;
        this.doctorService = doctorService;
    }

    private SpecialtieDoctorDTO toDTO(SpecialtieDoctorDomain specialtieDoctor) {
        return new SpecialtieDoctorDTO(
                specialtieDoctor.getId(),
                specialtieDoctor.getDoctor().getId(),
                specialtieDoctor.getIdSpecialtie()
        );
    }

    // Convierte el DTO recibido a la entidad de base de datos.
    // Busca el doctor real con DoctorService.findById: si el idDoctor que mandaron no existe, esto lanza BusinessException
    private SpecialtieDoctorDomain toDomain(SpecialtieDoctorDTO specialtieDoctor) {
        DoctorDomain doctor = doctorService.findById(specialtieDoctor.getDoctor());
        return new SpecialtieDoctorDomain(specialtieDoctor.getId(), doctor, specialtieDoctor.getSpecialtie());
    }

    // Devuelve todas las relaciones doctor-especialidad.
    @GetMapping
    public ResponseEntity<List<SpecialtieDoctorDTO>> findAllSpecialtieDoctor() {
        List<SpecialtieDoctorDTO> specialtieDoctors = specialtieDoctorService.findAll().stream()
                .map(this::toDTO)
                .toList();

        return ResponseEntity.ok(specialtieDoctors);
    }

    // Busca una relacion puntual por su propio id.
    @GetMapping("/{id}")
    public ResponseEntity<Object> findById(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(toDTO(specialtieDoctorService.findById(id)));
        } catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(MESSAGE_KEY, e.getMessage()));
        }
    }

    // Filtra relaciones por doctor o por especialidad (ambos parametros son opcionales; si no se manda ninguno, devuelve todas).
    @GetMapping("/filter")
    public ResponseEntity<List<SpecialtieDoctorDTO>> findByIdFilter(
            @RequestParam(required = false) UUID idDoctor,
            @RequestParam(required = false) UUID idSpecialtie) {

        List<SpecialtieDoctorDTO> specialtieDoctors = specialtieDoctorService.findByFilter(idDoctor, idSpecialtie).stream()
                .map(this::toDTO)
                .toList();

        return ResponseEntity.ok(specialtieDoctors);
    }

    // Crea una nueva relacion doctor-especialidad. Si el idDoctor enviado no corresponde a un doctor real, toDomain()
    // lanza BusinessException y se responde 404 con mensaje claro.
    @PostMapping
    public ResponseEntity<Object> createNewSpecialtieDoctor(@Valid @RequestBody SpecialtieDoctorDTO specialtieDoctor) {
        try {
            SpecialtieDoctorDomain createdSpecialtieDoctor = specialtieDoctorService.createNewSpecialtieDoctor(toDomain(specialtieDoctor));
            return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(createdSpecialtieDoctor));
        } catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(MESSAGE_KEY, e.getMessage()));
        }
    }

    // Actualiza una relacion existente (puede cambiar el doctor o la especialidad).
    @PutMapping
    public ResponseEntity<Object> updateSpecialtieDoctor(
            @RequestParam(required = true) UUID id,
            @Valid @RequestBody SpecialtieDoctorDTO specialtieDoctor) {

        try {
            SpecialtieDoctorDomain updatedSpecialtieDoctor = specialtieDoctorService.updateSpecialtieDoctor(id, toDomain(specialtieDoctor));
            return ResponseEntity.ok(toDTO(updatedSpecialtieDoctor));
        } catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(MESSAGE_KEY, e.getMessage()));
        }
    }

    // "Desactivar" aqui es borrar la fila de verdad: esta tabla no tiene un
    // campo active, porque la relacion en si no tiene sentido si no esta vigente.
    @PutMapping("/deactivate")
    public ResponseEntity<Object> deactivateSpecialtieDoctor(@RequestParam(required = true) UUID id) {
        try {
            specialtieDoctorService.deactivateSpecialtieDoctor(id);
            return ResponseEntity.noContent().build();
        } catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(MESSAGE_KEY, e.getMessage()));
        }
    }

}