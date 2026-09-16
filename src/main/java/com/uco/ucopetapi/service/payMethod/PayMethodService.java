package com.uco.ucopetapi.service.payMethod;

import com.uco.ucopetapi.domain.payMethod.PayMethodDomain;
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

    public List<PayMethodDomain> getAll() {
        return payMethodRepository.findAll();
    }

    public PayMethodDomain findByName(String name) {
        return payMethodRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Metodo de pago no encontrado con el nombre: " + name));
    }

    public PayMethodDomain findById(UUID id) {
        return payMethodRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Metodo de pago no encontrado con el id: " + id));
    }
}