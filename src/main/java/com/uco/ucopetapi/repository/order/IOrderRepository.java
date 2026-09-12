package com.uco.ucopetapi.repository.order;

import com.uco.ucopetapi.domain.order.OrderDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IOrderRepository extends JpaRepository<OrderDomain, UUID> {
}
