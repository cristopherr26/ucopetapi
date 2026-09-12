package com.uco.ucopetapi.controllers.payMethod;

import com.uco.ucopetapi.domain.payMethod.PayMethodDomain;
import com.uco.ucopetapi.service.payMethod.PayMethodService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/v1")
public class PayMethodController {

    private final PayMethodService payMethodService;

    public PayMethodController(PayMethodService payMethodService) {
        this.payMethodService = payMethodService;
    }

    @GetMapping("/metodosDePago")
    public ResponseEntity<List<PayMethodDomain>> getAllPayMethods() {
        return ResponseEntity.ok(payMethodService.obtenerTodos());
    }
}
