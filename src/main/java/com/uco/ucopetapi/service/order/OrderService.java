package com.uco.ucopetapi.service.order;

import com.uco.ucopetapi.domain.order.OrderDomain;
import com.uco.ucopetapi.dto.pets.PetDTO;
import com.uco.ucopetapi.repository.order.IOrderRepository;
import com.uco.ucopetapi.service.pet.PetService;
import com.uco.ucopetapi.service.procedure.ProcedureService;
import com.uco.ucopetapi.service.tutorPet.TutorPetService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService {

    private final IOrderRepository orderRepository;
    private final PetService petService;
    private final ProcedureService procedureService;
    private final TutorPetService tutorPetService;

    public OrderService(IOrderRepository orderRepository, PetService petService,
                        ProcedureService procedureService, TutorPetService tutorPetService) {
        this.orderRepository = orderRepository;
        this.petService = petService;
        this.procedureService = procedureService;
        this.tutorPetService = tutorPetService;
    }

    public List<OrderDomain> findAll() {
        return orderRepository.findAll();
    }

    public Optional<OrderDomain> findById(UUID id) {
        return orderRepository.findById(id);
    }

    public OrderDomain save(OrderDomain order) {
        if (order.getPetId() == null) {
            throw new IllegalArgumentException("El ID de la mascota es obligatorio para crear la orden.");
        }

        if (order.getProcedureId() == null) {
            throw new IllegalArgumentException("El ID del procedimiento es obligatorio para crear la orden.");
        }

        if (order.getState() == null) {
            order.setState("PENDIENTE");
        }
        if (order.getAuthorized() == null) {
            order.setAuthorized(false);
        }

        procedureService.findById(order.getProcedureId());
        PetDTO pet = petService.getById(order.getPetId());
        tutorPetService.findById(pet.getTutorId());

        order.setTutorId(pet.getTutorId());
        order.setIdOrder(generateNextIdOrder());

        if (order.getDate() == null) {
            order.setDate(LocalDateTime.now(ZoneId.of("America/Bogota")));
        }

        return orderRepository.save(order);
    }

    public OrderDomain changeProcedure(UUID id, UUID newProcedureId) {
        if (newProcedureId == null) {
            throw new IllegalArgumentException("El ID del nuevo procedimiento es obligatorio.");
        }

        return orderRepository.findById(id).map(order -> {
            if (!"PENDIENTE".equalsIgnoreCase(order.getState())) {
                throw new IllegalStateException("No se puede cambiar el procedimiento de una orden que ya fue procesada.");
            }

            procedureService.findById(newProcedureId);

            order.setProcedureId(newProcedureId);
            return orderRepository.save(order);
        }).orElseThrow(() -> new RuntimeException("Orden no encontrada con el ID: " + id));
    }

    public OrderDomain processAuthorization(UUID id, boolean isApproved) {
        return orderRepository.findById(id).map(order -> {
            if (!"PENDIENTE".equalsIgnoreCase(order.getState())) {
                throw new IllegalStateException("La orden ya fue autorizada o rechazada previamente.");
            }

            order.setAuthorized(isApproved);
            order.setState(isApproved ? "AUTORIZADO" : "DENEGADO");

            return orderRepository.save(order);
        }).orElseThrow(() -> new RuntimeException("Orden no encontrada con el ID: " + id));
    }

    public void delete(UUID id) {
        if (!orderRepository.existsById(id)) {
            throw new RuntimeException("No existe la orden con id: " + id);
        }
        orderRepository.deleteById(id);
    }

    private String generateNextIdOrder() {
        return orderRepository.findTopByOrderByDateDesc()
                .map(lastOrder -> {
                    try {
                        String currentCode = lastOrder.getIdOrder();
                        if (currentCode != null && currentCode.startsWith("ORD-")) {
                            int number = Integer.parseInt(currentCode.replace("ORD-", ""));
                            return "ORD-" + (number + 1);
                        }
                    } catch (Exception e) {
                        // Fallback en caso de que la cadena previa no sea numérica
                    }
                    return "ORD-" + (orderRepository.count() + 1);
                })
                .orElse("ORD-1");
    }
}