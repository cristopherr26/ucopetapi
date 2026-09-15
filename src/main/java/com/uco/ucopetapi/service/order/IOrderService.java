package com.uco.ucopetapi.service.order;

import com.uco.ucopetapi.domain.order.OrderDomain;
import java.util.List;
import java.util.UUID;

public interface IOrderService {

    List<OrderDomain> findAll();

    OrderDomain findById(UUID id);

    OrderDomain save(OrderDomain order);

    OrderDomain changeProcedure(UUID id, UUID newProcedureId);

    OrderDomain processAuthorization(UUID id, boolean isApproved);

    void delete(UUID id);
}