package com.uco.ucopetapi.dto.sale;

import java.util.UUID;

public record SaleDTO(
    String id,
    String productName,
    int quantity,
    Double price
) {

}
