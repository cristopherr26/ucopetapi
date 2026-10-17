package com.uco.ucopetapi.service.attentionhealth;

import com.uco.ucopetapi.dto.attentionhealth.AttentionHealthDTO;

import java.util.List;
import java.util.UUID;

/**
 * Service contract consumed by the new attention Controller.
 */
public interface AttentionHealthService {

    AttentionHealthDTO create(AttentionHealthDTO request);

    AttentionHealthDTO findById(Long attentionId);

    List<AttentionHealthDTO> findByPetId(UUID petId);

    AttentionHealthDTO update(Long attentionId, AttentionHealthDTO request);

    AttentionHealthDTO start(Long attentionId);

    AttentionHealthDTO cancel(Long attentionId);

    AttentionHealthDTO finish(Long attentionId);
}


