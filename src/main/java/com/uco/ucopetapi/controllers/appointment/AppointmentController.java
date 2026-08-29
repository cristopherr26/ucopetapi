package com.uco.ucopetapi.controllers.appointment;

import com.uco.ucopetapi.dto.appointment.AppointmentDTO;
import com.uco.ucopetapi.dto.appointmentType.AppointmentTypeDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/appointments")
public class AppointmentController {

    @GetMapping
    public ResponseEntity<List<AppointmentDTO>> get() {
        return ResponseEntity.ok(Collections.emptyList());
    }

    @GetMapping("/appointment")
    public ResponseEntity<AppointmentDTO> get(@RequestParam(required = true) UUID id) {
        AppointmentDTO appointment = new AppointmentDTO();
        appointment.setId(id);
        return ResponseEntity.ok(appointment);
    }

    @PostMapping("/appointment")
    public ResponseEntity<AppointmentDTO> create(@RequestBody AppointmentDTO appointmentDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentDTO);
    }

    @PutMapping("/appointment")
    public ResponseEntity<AppointmentDTO> update(
            @RequestParam(required = true) UUID id,
            @RequestBody AppointmentDTO appointmentDTO) {
        appointmentDTO.setId(id);
        return ResponseEntity.ok(appointmentDTO);
    }

    @DeleteMapping("/appointment")
    public ResponseEntity<Void> delete(@RequestParam(required = true) UUID id) {
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/appointment-type")
    public ResponseEntity<List<AppointmentTypeDTO>> getAppointmentTypes() {
        return ResponseEntity.ok(Collections.emptyList());
    }

    @GetMapping("/appointment-type/detail")
    public ResponseEntity<AppointmentTypeDTO> getAppointmentType(@RequestParam(required = true) UUID id) {
        AppointmentTypeDTO appointmentType = new AppointmentTypeDTO();
        appointmentType.setId(id);
        return ResponseEntity.ok(appointmentType);
    }

    @PostMapping("/appointment-type")
    public ResponseEntity<AppointmentTypeDTO> createAppointmentType(@RequestBody AppointmentTypeDTO appointmentTypeDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentTypeDTO);
    }

    @PutMapping("/appointment-type")
    public ResponseEntity<AppointmentTypeDTO> updateAppointmentType(
            @RequestParam(required = true) UUID id,
            @RequestBody AppointmentTypeDTO appointmentTypeDTO) {
        appointmentTypeDTO.setId(id);
        return ResponseEntity.ok(appointmentTypeDTO);
    }

    @DeleteMapping("/appointment-type")
    public ResponseEntity<Void> deleteAppointmentType(@RequestParam(required = true) UUID id) {
        return ResponseEntity.noContent().build();
    }
}
