package com.uco.ucopetapi.controllers.appointment;

import com.uco.ucopetapi.dto.appointment.AppointmentDTO;
import com.uco.ucopetapi.dto.appointment.AppointmentDoctorDTO;
import com.uco.ucopetapi.dto.appointment.AppointmentStatusDTO;
import com.uco.ucopetapi.dto.appointmentType.AppointmentTypeDTO;
import com.uco.ucopetapi.dto.appointmentType.AppointmentTypeStatusDTO;
import com.uco.ucopetapi.service.appointment.AppointmentAccessPolicy;
import com.uco.ucopetapi.service.appointment.AppointmentService;
import com.uco.ucopetapi.service.appointmentType.AppointmentTypeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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

@RestController
@RequestMapping("/api/v1/appointments")
public class AppointmentController {

    private static final String ADMIN_ONLY_TYPES = "Solo un administrador puede gestionar los tipos de cita";
    private static final String ADMIN_ONLY_DELETE = "Solo un administrador puede eliminar citas";

    private final AppointmentService appointmentService;
    private final AppointmentTypeService appointmentTypeService;
    private final AppointmentAccessPolicy accessPolicy;

    public AppointmentController(
            AppointmentService appointmentService,
            AppointmentTypeService appointmentTypeService,
            AppointmentAccessPolicy accessPolicy) {
        this.appointmentService = appointmentService;
        this.appointmentTypeService = appointmentTypeService;
        this.accessPolicy = accessPolicy;
    }

    // Un DOCTOR solo recibe sus propias citas; el ADMIN recibe todas y puede filtrar por doctorId.
    @GetMapping
    public ResponseEntity<List<AppointmentDTO>> get(
            @RequestParam(required = false) UUID tutorId,
            @RequestParam(required = false) UUID doctorId) {
        List<AppointmentDTO> appointments = tutorId == null
                ? appointmentService.findAll()
                : appointmentService.findByTutorId(tutorId);
        List<AppointmentDTO> visible = accessPolicy.visibleTo(appointments);
        if (doctorId != null) {
            visible = visible.stream()
                    .filter(appointment -> doctorId.equals(appointment.getDoctorId()))
                    .toList();
        }
        return ResponseEntity.ok(visible);
    }

    // El medico de quien tiene la sesion abierta (y su sede). 404 si la persona no es medico.
    @GetMapping("/me/doctor")
    public ResponseEntity<AppointmentDoctorDTO> getOwnDoctor() {
        return accessPolicy.ownDoctor()
                .map(doctor -> ResponseEntity.ok(
                        new AppointmentDoctorDTO(doctor.getId(), doctor.getHeadquarterId())))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/appointment")
    public ResponseEntity<AppointmentDTO> getAppointment(@RequestParam(required = true) UUID id) {
        accessPolicy.requireOwnAppointment(id);
        return appointmentService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/pending")
    public ResponseEntity<List<AppointmentDTO>> getPendingAppointments(@RequestParam(required = true) UUID tutorId) {
        return ResponseEntity.ok(accessPolicy.visibleTo(appointmentService.findPendingByTutorId(tutorId)));
    }

    // Crear, actualizar y cambiar de estado son ASINCRONOS: la API valida y publica en RabbitMQ,
    // y el procesador de salud guarda. Por eso responden 202 Accepted con el idMessage del mensaje.
    @PostMapping({"", "/", "/appointment"})
    public ResponseEntity<Map<String, String>> create(@RequestBody AppointmentDTO appointmentDTO) {
        accessPolicy.requireOwnDoctor(appointmentDTO.getDoctorId());
        String idMessage = appointmentService.create(appointmentDTO);
        return accepted("La creacion de la cita fue iniciada", idMessage, appointmentDTO.getId());
    }

    @PutMapping({"", "/", "/appointment"})
    public ResponseEntity<Map<String, String>> update(
            @RequestParam(required = true) UUID id,
            @RequestBody AppointmentDTO appointmentDTO) {
        accessPolicy.requireOwnAppointment(id);
        accessPolicy.requireOwnDoctor(appointmentDTO.getDoctorId());
        return appointmentService.update(id, appointmentDTO)
                .map(idMessage -> accepted("La actualizacion de la cita fue iniciada", idMessage, id))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Map<String, String>> updateStatus(
            @PathVariable UUID id,
            @RequestBody AppointmentStatusDTO appointmentStatusDTO) {
        accessPolicy.requireOwnAppointment(id);
        return appointmentService.requestStatusChange(id, appointmentStatusDTO)
                .map(idMessage -> accepted("El cambio de estado de la cita fue iniciado", idMessage, id))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping({"", "/", "/appointment"})
    public ResponseEntity<Void> delete(@RequestParam(required = true) UUID id) {
        accessPolicy.requireAdmin(ADMIN_ONLY_DELETE);
        return appointmentService.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/appointment-type")
    public ResponseEntity<List<AppointmentTypeDTO>> getAppointmentTypes() {
        return ResponseEntity.ok(appointmentTypeService.findAll());
    }

    @GetMapping("/appointment-type/active")
    public ResponseEntity<List<AppointmentTypeDTO>> getActiveAppointmentTypes() {
        return ResponseEntity.ok(appointmentTypeService.findAllActive());
    }

    @GetMapping("/appointment-type/detail")
    public ResponseEntity<AppointmentTypeDTO> getAppointmentType(@RequestParam(required = true) UUID id) {
        return appointmentTypeService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/appointment-type")
    public ResponseEntity<AppointmentTypeDTO> createAppointmentType(@RequestBody AppointmentTypeDTO appointmentTypeDTO) {
        accessPolicy.requireAdmin(ADMIN_ONLY_TYPES);
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentTypeService.create(appointmentTypeDTO));
    }

    @PutMapping("/appointment-type")
    public ResponseEntity<AppointmentTypeDTO> updateAppointmentType(
            @RequestParam(required = true) UUID id,
            @RequestBody AppointmentTypeDTO appointmentTypeDTO) {
        accessPolicy.requireAdmin(ADMIN_ONLY_TYPES);
        return appointmentTypeService.update(id, appointmentTypeDTO)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/appointment-type/{id}/status")
    public ResponseEntity<AppointmentTypeDTO> updateAppointmentTypeStatus(
            @PathVariable UUID id,
            @RequestBody AppointmentTypeStatusDTO appointmentTypeStatusDTO) {
        accessPolicy.requireAdmin(ADMIN_ONLY_TYPES);
        return appointmentTypeService.updateStatus(id, appointmentTypeStatusDTO)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/appointment-type")
    public ResponseEntity<Void> deleteAppointmentType(@RequestParam(required = true) UUID id) {
        accessPolicy.requireAdmin(ADMIN_ONLY_TYPES);
        return appointmentTypeService.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    private ResponseEntity<Map<String, String>> accepted(String message, String idMessage, UUID id) {
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(Map.of("message", message, "idMessage", idMessage, "id", id.toString()));
    }
}
