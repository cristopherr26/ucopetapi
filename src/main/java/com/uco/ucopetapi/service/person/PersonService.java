package com.uco.ucopetapi.service.person;

import java.util.List;
import java.util.UUID;

import com.uco.ucopetapi.dto.person.ChangePasswordRequestDTO;
import com.uco.ucopetapi.dto.person.DocumentType;
import com.uco.ucopetapi.dto.person.LoginRequestDTO;
import com.uco.ucopetapi.dto.person.LoginResponseDTO;
import com.uco.ucopetapi.dto.person.PersonDTO;
import com.uco.ucopetapi.dto.person.SetPasswordRequestDTO;

public interface PersonService {
    List<PersonDTO> findAll();

    PersonDTO findById(UUID personId);

    PersonDTO findByDocument(DocumentType documentType, String documentNumber);

    PersonDTO findByEmail(String email);

    PersonDTO create(PersonDTO person);

    PersonDTO update(UUID personId, PersonDTO person);

    void delete(UUID personId);

    void logout(UUID personId);

    void setPassword(UUID personId, SetPasswordRequestDTO request);

    void changeOwnPassword(UUID personId, ChangePasswordRequestDTO request);

    LoginResponseDTO login(LoginRequestDTO request);
}
