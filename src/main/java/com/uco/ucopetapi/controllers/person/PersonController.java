package com.uco.ucopetapi.controllers.person;

import java.util.List;
import java.util.UUID;

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

import com.uco.ucopetapi.dto.person.PersonDTO;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/rest")
public class PersonController {

    private static final UUID EJEMPLO = UUID.fromString("3f2a8c1e-4b5d-4e2a-9c11-77f1a0c4b9de");

    @GetMapping("/person")
    public PersonDTO get(@RequestParam(required = true) String documentType,
                         @RequestParam(required = true) String documentNumber) {
        return new PersonDTO(EJEMPLO, documentType, documentNumber,
                "Ana Maria", "Rios", "ana.rios@correo.com", "3000000000", true);
    }

    @GetMapping("/person/email")
    public PersonDTO getByEmail(@RequestParam(required = true) String email) {
        return new PersonDTO(EJEMPLO, "CC", "1036442118",
                "Ana Maria", "Rios", email, "3000000000", true);
    }

    @GetMapping("/persons")
    public List<PersonDTO> findAll() {
        return List.of(
                new PersonDTO(EJEMPLO, "CC", "1036442118",
                        "Ana Maria", "Rios", "ana.rios@correo.com", "3000000000", true),
                new PersonDTO(UUID.randomUUID(), "CC", "71884203",
                        "Carlos", "Pena Duque", "carlos.pena@correo.com", "3015557788", true));
    }

    @PostMapping("/person")
    public ResponseEntity<PersonDTO> create(@Valid @RequestBody PersonDTO person) {
        // El id lo asigna el servidor, nunca el cliente.
        PersonDTO creada = new PersonDTO(UUID.randomUUID(), person.documentType(),
                person.documentNumber(), person.firstName(), person.lastName(),
                person.email(), person.phone(), true);
        return new ResponseEntity<>(creada, HttpStatus.CREATED);
    }

    @PutMapping("/person")
    public ResponseEntity<PersonDTO> update(@RequestParam(required = true) UUID personId,
                                            @Valid @RequestBody PersonDTO person) {
        PersonDTO actualizada = new PersonDTO(personId, person.documentType(),
                person.documentNumber(), person.firstName(), person.lastName(),
                person.email(), person.phone(), person.active());
        return new ResponseEntity<>(actualizada, HttpStatus.OK);
    }

    @DeleteMapping("/person")
    public ResponseEntity<Void> delete(@RequestParam(required = true) UUID personId) {
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
