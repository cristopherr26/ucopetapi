package com.uco.ucopetapi.controllers.person;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.uco.ucopetapi.dto.person.ChangePasswordRequestDTO;
import com.uco.ucopetapi.dto.person.DocumentType;
import com.uco.ucopetapi.dto.person.DocumentTypeDTO;
import com.uco.ucopetapi.dto.person.LoginRequestDTO;
import com.uco.ucopetapi.dto.person.LoginResponseDTO;
import com.uco.ucopetapi.dto.person.PersonDTO;
import com.uco.ucopetapi.dto.person.SetPasswordRequestDTO;
import com.uco.ucopetapi.service.person.PersonService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/persons")
public class PersonController {
    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @GetMapping
    public List<PersonDTO> findAll() {
        return personService.findAll();
    }

    @GetMapping("/document-types")
    public List<DocumentTypeDTO> documentTypes() {
        return Arrays.stream(DocumentType.values()).map(DocumentTypeDTO::de).toList();
    }

    @GetMapping(params = {"documentType", "documentNumber"})
    public PersonDTO findByDocument(@RequestParam DocumentType documentType,
                                    @RequestParam String documentNumber) {
        return personService.findByDocument(documentType, documentNumber);
    }

    @GetMapping(params = "email")
    public PersonDTO findByEmail(@RequestParam String email) {
        return personService.findByEmail(email);
    }

    @GetMapping("/me")
    public PersonDTO me(Authentication authentication) {
        return personService.findById(UUID.fromString(authentication.getName()));
    }

    @GetMapping("/{personId}")
    public PersonDTO findById(@PathVariable UUID personId) {
        return personService.findById(personId);
    }

    @PostMapping
    public ResponseEntity<PersonDTO> create(@Valid @RequestBody PersonDTO person) {
        return new ResponseEntity<>(personService.create(person), HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN') or #personId.toString() == authentication.name")
    @PutMapping("/{personId}")
    public ResponseEntity<PersonDTO> update(@PathVariable UUID personId,
                                            @Valid @RequestBody PersonDTO person) {
        return new ResponseEntity<>(personService.update(personId, person), HttpStatus.OK);
    }

    @DeleteMapping("/{personId}")
    public ResponseEntity<Void> delete(@PathVariable UUID personId) {
        personService.delete(personId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changeOwnPassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequestDTO request) {
        personService.changeOwnPassword(UUID.fromString(authentication.getName()), request);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{personId}/password")
    public ResponseEntity<Void> setPassword(@PathVariable UUID personId,
                                            @Valid @RequestBody SetPasswordRequestDTO request) {
        personService.setPassword(personId, request);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Authentication authentication) {
        personService.logout(UUID.fromString(authentication.getName()));
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/login")
    public LoginResponseDTO login(@Valid @RequestBody LoginRequestDTO request) {
        return personService.login(request);
    }
}
