package com.uco.ucopetapi.service.procedure;

import com.uco.ucopetapi.dto.procedure.ProcedureDTO;
import com.uco.ucopetapi.dto.space.SpaceDTO;

import java.util.List;
import java.util.UUID;

public interface IProcedureService {

    List<ProcedureDTO> findAll();

    List<ProcedureDTO> findActiveProcedures();

    List<ProcedureDTO> findInactiveProcedures();

    List<SpaceDTO> findActiveSpacesForProcedureCreation();

    ProcedureDTO findById(UUID id);

    ProcedureDTO findByCode(String code);

    String create(ProcedureDTO request);

    String update(UUID id, ProcedureDTO request);

    String deactivate(UUID id);

    String activate(UUID id);
}