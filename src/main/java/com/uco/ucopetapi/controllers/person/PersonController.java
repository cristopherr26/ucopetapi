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
import com.uco.ucopetapi.services.person.PersonService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/rest")
public class PersonController {

    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @GetMapping("/person")
    public PersonDTO get(@RequestParam(required = true) String documentType,
                         @RequestParam(required = true) String documentNumber) {
        return personService.getByDocument(documentType, documentNumber);
    }

    @GetMapping("/person/email")
    public PersonDTO getByEmail(@RequestParam(required = true) String email) {
        return personService.getByEmail(email);
    }

    @GetMapping("/persons")
    public List<PersonDTO> findAll() {
        return personService.findAll();
    }

    @PostMapping("/person")
    public ResponseEntity<PersonDTO> create(@Valid @RequestBody PersonDTO person) {
        return new ResponseEntity<>(personService.create(person), HttpStatus.CREATED);
    }

    @PutMapping("/person")
    public ResponseEntity<PersonDTO> update(@RequestParam(required = true) UUID personId,
                                            @Valid @RequestBody PersonDTO person) {
        return new ResponseEntity<>(personService.update(personId, person), HttpStatus.OK);
    }

    @DeleteMapping("/person")
    public ResponseEntity<Void> delete(@RequestParam(required = true) UUID personId) {
        personService.deactivate(personId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
