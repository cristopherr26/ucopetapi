package com.uco.ucopetapi.service.headquarter;

import com.uco.ucopetapi.domain.headquarter.HeadquarterDomain;
import com.uco.ucopetapi.domain.headquarter.mapper.HeadquarterMapper;
import com.uco.ucopetapi.dto.headquarter.HeadquarterDTO;
import com.uco.ucopetapi.exception.BusinessException;
import com.uco.ucopetapi.messaging.headquarter.HeadquarterCommand;
import com.uco.ucopetapi.messaging.headquarter.SaveHeadquarterCommand;
import com.uco.ucopetapi.repository.headquarter.HeadquarterRepository;
import com.uco.ucopetapi.util.MessageSender;
import org.hibernate.internal.util.Optional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class HeadquarterService {

    private static final String HEADQUARTER_NOT_FOUND_MSG = "No se encontró la sede con el ID: ";

    private final HeadquarterRepository headquarterRepository;
    private final HeadquarterMapper headquarterMapper;
    private final MessageSender<HeadquarterCommand> headquarterSender;

    public HeadquarterService(HeadquarterRepository headquarterRepository, HeadquarterMapper headquarterMapper, MessageSender<HeadquarterCommand> headquarterSender) {
        this.headquarterRepository = headquarterRepository;
        this.headquarterMapper = headquarterMapper;
        this.headquarterSender = headquarterSender;
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
        UUID id = UUID.randomUUID();
        Boolean isActive = dto.getIsActive() == null ? Boolean.TRUE : dto.getIsActive();

        publish(new SaveHeadquarterCommand(
                id,
                dto.getName(),
                dto.getAddress(),
                isActive,
                Instant.now()
        ));
        dto.setId(id);
        dto.setIsActive(isActive);
        return dto;
    }

    public HeadquarterDTO updateHeadquarter(UUID id, HeadquarterDTO dto) {
        
    }

    public HeadquarterDTO deactivateHeadquarter(UUID id) {
        HeadquarterDomain existing = headquarterRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HEADQUARTER_NOT_FOUND_MSG + id));

        existing.setIsActive(false);

        HeadquarterDomain deactivated = headquarterRepository.save(existing);
        return headquarterMapper.toDTO(deactivated);
    }

    private void publish(HeadquarterCommand command) {
        headquarterSender.execute(command, UUID.randomUUID().toString());
    }
}