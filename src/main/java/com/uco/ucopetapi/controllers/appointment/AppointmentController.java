package com.uco.ucopetapi.controllers.appointment;

import com.uco.ucopetapi.dto.appointment.AppointmentDTO;
import com.uco.ucopetapi.dto.appointment.AppointmentStatusDTO;
import com.uco.ucopetapi.dto.appointmentType.AppointmentTypeDTO;
import com.uco.ucopetapi.dto.appointmentType.AppointmentTypeStatusDTO;
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

    private final AppointmentService appointmentService;
    private final AppointmentTypeService appointmentTypeService;

    public AppointmentController(
            AppointmentService appointmentService,
            AppointmentTypeService appointmentTypeService) {
        this.appointmentService = appointmentService;
        this.appointmentTypeService = appointmentTypeService;
    }

    @GetMapping
    public ResponseEntity<List<AppointmentDTO>> get(@RequestParam(required = false) UUID tutorId) {
        List<AppointmentDTO> appointments = tutorId == null
                ? appointmentService.findAll()
                : appointmentService.findByTutorId(tutorId);
        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/appointment")
    public ResponseEntity<AppointmentDTO> getAppointment(@RequestParam(required = true) UUID id) {
        return appointmentService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/pending")
    public ResponseEntity<List<AppointmentDTO>> getPendingAppointments(@RequestParam(required = true) UUID tutorId) {
        return ResponseEntity.ok(appointmentService.findPendingByTutorId(tutorId));
    }

    // Crear, actualizar y cambiar de estado son ASINCRONOS: la API valida y publica en RabbitMQ,
    // y el procesador de salud guarda. Por eso responden 202 Accepted con el idMessage del mensaje.
    @PostMapping({"", "/", "/appointment"})
    public ResponseEntity<Map<String, String>> create(@RequestBody AppointmentDTO appointmentDTO) {
        String idMessage = appointmentService.create(appointmentDTO);
        return accepted("La creacion de la cita fue iniciada", idMessage, appointmentDTO.getId());
    }

    @PutMapping({"", "/", "/appointment"})
    public ResponseEntity<Map<String, String>> update(
            @RequestParam(required = true) UUID id,
            @RequestBody AppointmentDTO appointmentDTO) {
        return appointmentService.update(id, appointmentDTO)
                .map(idMessage -> accepted("La actualizacion de la cita fue iniciada", idMessage, id))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Map<String, String>> updateStatus(
            @PathVariable UUID id,
            @RequestBody AppointmentStatusDTO appointmentStatusDTO) {
        return appointmentService.requestStatusChange(id, appointmentStatusDTO)
                .map(idMessage -> accepted("El cambio de estado de la cita fue iniciado", idMessage, id))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping({"", "/", "/appointment"})
    public ResponseEntity<Void> delete(@RequestParam(required = true) UUID id) {
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
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentTypeService.create(appointmentTypeDTO));
    }

    @PutMapping("/appointment-type")
    public ResponseEntity<AppointmentTypeDTO> updateAppointmentType(
            @RequestParam(required = true) UUID id,
            @RequestBody AppointmentTypeDTO appointmentTypeDTO) {
        return appointmentTypeService.update(id, appointmentTypeDTO)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/appointment-type/{id}/status")
    public ResponseEntity<AppointmentTypeDTO> updateAppointmentTypeStatus(
            @PathVariable UUID id,
            @RequestBody AppointmentTypeStatusDTO appointmentTypeStatusDTO) {
        return appointmentTypeService.updateStatus(id, appointmentTypeStatusDTO)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/appointment-type")
    public ResponseEntity<Void> deleteAppointmentType(@RequestParam(required = true) UUID id) {
        return appointmentTypeService.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    private ResponseEntity<Map<String, String>> accepted(String message, String idMessage, UUID id) {
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(Map.of("message", message, "idMessage", idMessage, "id", id.toString()));
    }
}
