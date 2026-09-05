package com.uco.ucopetapi.controllers.vitalSigns;
import com.uco.ucopetapi.dto.vitalSigns.VitalSignsDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/vitalsigns")
public class VitalSignsController {

    private VitalSignsDTO buildVitalSigns() {
        return new VitalSignsDTO(
                38.5,
                90,
                24,
                120,
                80,
                12.4,
                5,
                LocalDateTime.of(2026, 8, 27, 10, 30)
        );
    }

    @GetMapping
    public ResponseEntity<List<VitalSignsDTO>> findAll() {
        return ResponseEntity.ok(List.of(buildVitalSigns()));
    }

    @PostMapping
    public ResponseEntity<VitalSignsDTO> createVitalSigns(@RequestBody VitalSignsDTO request) {
        VitalSignsDTO created = new VitalSignsDTO(
                request.getTemperature(),
                request.getHeartRate(),
                request.getRespiratoryRate(),
                request.getSystolicPressure(),
                request.getDiastolicPressure(),
                request.getWeight(),
                request.getBodyConditionScore(),
                request.getMeasurementDate() != null ? request.getMeasurementDate() : LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping
    public ResponseEntity<VitalSignsDTO> updateVitalSigns(@RequestBody VitalSignsDTO request) {
        VitalSignsDTO updated = new VitalSignsDTO(
                request.getTemperature(),
                request.getHeartRate(),
                request.getRespiratoryRate(),
                request.getSystolicPressure(),
                request.getDiastolicPressure(),
                request.getWeight(),
                request.getBodyConditionScore(),
                request.getMeasurementDate() != null ? request.getMeasurementDate() : LocalDateTime.now()
        );
        return ResponseEntity.ok(updated);
    }
}