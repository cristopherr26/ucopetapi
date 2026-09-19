package com.uco.ucopetapi.service.headquarter;

import com.uco.ucopetapi.domain.headquarter.HeadquarterDomain;
import com.uco.ucopetapi.domain.headquarter.mapper.HeadquarterMapper;
import com.uco.ucopetapi.dto.headquarter.HeadquarterDTO;
import com.uco.ucopetapi.exception.BusinessException;
import com.uco.ucopetapi.repository.headquarter.HeadquarterRepository;
import org.hibernate.internal.util.Optional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class HeadquarterService {

    private final HeadquarterRepository headquarterRepository;
    private final HeadquarterMapper headquarterMapper;
    private static final String HEADQUARTER_NOT_FOUND_MSG = "No se encontró la sede con el ID: ";

    public HeadquarterService(HeadquarterRepository headquarterRepository, HeadquarterMapper headquarterMapper) {
        this.headquarterRepository = headquarterRepository;
        this.headquarterMapper = headquarterMapper;
    }

    public List<HeadquarterDTO> getAllHeadquarters() {
        return headquarterRepository.findAll()
                .stream()
                .map(headquarterMapper::toDTO)
                .toList();
    }

    public HeadquarterDTO findById(UUID id) {
        HeadquarterDomain domain = headquarterRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HEADQUARTER_NOT_FOUND_MSG + id));

        return headquarterMapper.toDTO(domain);
    }

    public HeadquarterDTO createNewHeadquarter(HeadquarterDTO dto) {
        HeadquarterDomain domain = headquarterMapper.toDomain(dto);

        if (domain.getIsActive() == null) {
            domain.setIsActive(true);
        }

        HeadquarterDomain saved = headquarterRepository.save(domain);
        return headquarterMapper.toDTO(saved);
    }

    public HeadquarterDTO updateHeadquarter(UUID id, HeadquarterDTO dto) {
        HeadquarterDomain existing = headquarterRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HEADQUARTER_NOT_FOUND_MSG + id));

        existing.setName(dto.getName());
        existing.setAddress(dto.getAddress());

        if (dto.getIsActive() != null) {
            existing.setIsActive(dto.getIsActive());
        }

        HeadquarterDomain updated = headquarterRepository.save(existing);
        return headquarterMapper.toDTO(updated);
    }

    public HeadquarterDTO deactivateHeadquarter(UUID id) {
        HeadquarterDomain existing = headquarterRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HEADQUARTER_NOT_FOUND_MSG + id));

        existing.setIsActive(false);

        HeadquarterDomain deactivated = headquarterRepository.save(existing);
        return headquarterMapper.toDTO(deactivated);
    }
}