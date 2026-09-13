package com.uco.ucopetapi.service.payMethod;

import com.uco.ucopetapi.domain.payMethod.PayMethodDomain;
import com.uco.ucopetapi.repository.payMethod.PayMethodRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PayMethodService {

    private final PayMethodRepository payMethodRepository;

    public PayMethodService(PayMethodRepository payMethodRepository) {
        this.payMethodRepository = payMethodRepository;
    }

    public List<PayMethodDomain> obtenerTodos() {
        return payMethodRepository.findAll();
    }

}