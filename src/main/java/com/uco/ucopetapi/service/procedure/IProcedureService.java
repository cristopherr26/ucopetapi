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

    ProcedureDTO create(ProcedureDTO request);

    ProcedureDTO update(UUID id, ProcedureDTO request);

    ProcedureDTO deactivate(UUID id);

    ProcedureDTO activate(UUID id);
}