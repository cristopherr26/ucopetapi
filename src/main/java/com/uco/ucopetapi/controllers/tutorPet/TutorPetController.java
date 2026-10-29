package com.uco.ucopetapi.controllers.tutorPet;

import com.uco.ucopetapi.dto.tutorPet.TutorPetDTO;
import com.uco.ucopetapi.exception.BusinessException;
import com.uco.ucopetapi.service.tutorPet.TutorPetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/tutorPet")
public class TutorPetController {

    private final TutorPetService tutorPetService;

    public TutorPetController(TutorPetService tutorPetService) {
        this.tutorPetService = tutorPetService;
    }

    @GetMapping
    public ResponseEntity<List<TutorPetDTO>> findAllTutorPet() {
        return ResponseEntity.ok(tutorPetService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TutorPetDTO> findTutorPetById(@PathVariable UUID id) {
        return ResponseEntity.ok(tutorPetService.findById(id));
    }

    @GetMapping("/filter")
    public ResponseEntity<List<TutorPetDTO>> findTutorPetByFilter(@RequestParam(required = true) UUID id) {
        return ResponseEntity.ok(tutorPetService.findByPersonId(id));
    }

    @PostMapping
    public ResponseEntity<TutorPetDTO> createNewTutorPet(@Valid @RequestBody TutorPetDTO tutorPet) {
        TutorPetDTO createdTutorPet = tutorPetService.create(tutorPet);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTutorPet);
    }

    @PutMapping
    public ResponseEntity<TutorPetDTO> updateTutorPet(
            @RequestParam(required = true) UUID id,
            @Valid @RequestBody TutorPetDTO tutorPet) {

        TutorPetDTO updatedTutorPet = tutorPetService.update(id, tutorPet);
        return ResponseEntity.ok(updatedTutorPet);
    }

    @PutMapping("/deactivate")
    public ResponseEntity<Void> deactivateTutorPet(@RequestParam(required = true) UUID id) {
        tutorPetService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<String> handleBusinessException(BusinessException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> handleRequestException(ResponseStatusException e) {
        return ResponseEntity.status(e.getStatusCode())
                .body(e.getReason() == null ? "La solicitud de TutorPet no es válida." : e.getReason());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .findFirst()
                .orElse("La solicitud de TutorPet contiene datos inválidos.");
        return ResponseEntity.badRequest().body(message);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<String> handleMissingParameter(MissingServletRequestParameterException e) {
        return ResponseEntity.badRequest().body("El parámetro '" + e.getParameterName() + "' es obligatorio.");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handleInvalidParameterType(MethodArgumentTypeMismatchException e) {
        return ResponseEntity.badRequest().body("El identificador de TutorPet o persona debe ser un UUID válido.");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleUnreadableBody(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest().body("El cuerpo de la solicitud está vacío o tiene un formato inválido.");
    }
}