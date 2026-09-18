package com.uco.ucopetapi.service.payMethod;

import com.uco.ucopetapi.domain.payMethod.PayMethodDomain;
import com.uco.ucopetapi.dto.payMethod.PayMethodDTO;
import com.uco.ucopetapi.repository.payMethod.PayMethodRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PayMethodService {

    private final PayMethodRepository payMethodRepository;

    public PayMethodService(PayMethodRepository payMethodRepository) {
        this.payMethodRepository = payMethodRepository;
    }

    public List<PayMethodDTO> getAll() {
        return payMethodRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public PayMethodDTO findByName(String name) {
        PayMethodDomain domain = payMethodRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Metodo de pago no encontrado con el nombre: " + name));
        return toDTO(domain);
    }

    public PayMethodDTO findById(UUID id) {
        PayMethodDomain domain = payMethodRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Metodo de pago no encontrado con el id: " + id));
        return toDTO(domain);
    }

    private PayMethodDTO toDTO(PayMethodDomain domain) {
        return new PayMethodDTO(domain.getId(), domain.getName());
    }
}