package com.levelup.order_service.controller;

import com.levelup.order_service.model.Order;
import com.levelup.order_service.model.OrderItem;
import com.levelup.order_service.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @GetMapping
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                item.setOrder(order);
            }
        }

        Order savedOrder = orderRepository.save(order);
        return ResponseEntity.ok(savedOrder);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        Order order = orderRepository.findById(id).orElse(null);

        if (order == null) {
            return ResponseEntity.notFound().build();
        }

        // Solo se puede eliminar si está en estado CREATED
        if (!"CREATED".equals(order.getStatus())) {
            return ResponseEntity.status(403).build();
        }

        // Verificar que no hayan pasado 24 horas
        if (isExpired(order)) {
            // Auto-confirmar si pasaron 24 horas
            order.setStatus("CONFIRMED");
            orderRepository.save(order);
            return ResponseEntity.status(403).build();
        }

        orderRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        Order order = orderRepository.findById(id).orElse(null);

        if (order == null) {
            return ResponseEntity.notFound().build();
        }

        // Auto-confirmar si pasaron 24 horas y sigue en CREATED
        if ("CREATED".equals(order.getStatus()) && isExpired(order)) {
            order.setStatus("CONFIRMED");
            orderRepository.save(order);
        }

        return ResponseEntity.ok(order);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Order> updateOrder(@PathVariable Long id, @RequestBody Order orderDetails) {
        Order existingOrder = orderRepository.findById(id).orElse(null);

        if (existingOrder == null) {
            return ResponseEntity.notFound().build();
        }

        // Solo se puede editar si está en estado CREATED
        if (!"CREATED".equals(existingOrder.getStatus())) {
            return ResponseEntity.status(403).body(existingOrder);
        }

        // Verificar que no hayan pasado 24 horas
        if (isExpired(existingOrder)) {
            // Auto-confirmar si pasaron 24 horas
            existingOrder.setStatus("CONFIRMED");
            orderRepository.save(existingOrder);
            return ResponseEntity.status(403).body(existingOrder);
        }

        // Actualizar campos permitidos
        existingOrder.setCustomerName(orderDetails.getCustomerName());
        existingOrder.setCustomerPhone(orderDetails.getCustomerPhone());
        existingOrder.setDeliveryAddress(orderDetails.getDeliveryAddress());
        existingOrder.setPaymentMethod(orderDetails.getPaymentMethod());

        // Si viene el status "CONFIRMED", confirmar el pedido manualmente
        if ("CONFIRMED".equals(orderDetails.getStatus())) {
            existingOrder.setStatus("CONFIRMED");
        }

        Order updatedOrder = orderRepository.save(existingOrder);
        return ResponseEntity.ok(updatedOrder);
    }

    // Método para confirmar manualmente un pedido
    @PatchMapping("/{id}/confirm")
    public ResponseEntity<Order> confirmOrder(@PathVariable Long id) {
        Order order = orderRepository.findById(id).orElse(null);

        if (order == null) {
            return ResponseEntity.notFound().build();
        }

        if ("CONFIRMED".equals(order.getStatus())) {
            return ResponseEntity.ok(order); // Ya está confirmado
        }

        order.setStatus("CONFIRMED");
        Order confirmedOrder = orderRepository.save(order);
        return ResponseEntity.ok(confirmedOrder);
    }

    // Método auxiliar: verificar si pasaron 24 horas desde la creación
    private boolean isExpired(Order order) {
        try {
            LocalDateTime createdAt = order.getCreatedAt();
            if (createdAt == null) {
                return false;
            }

            LocalDateTime now = LocalDateTime.now();
            long hoursPassed = ChronoUnit.HOURS.between(createdAt, now);

            return hoursPassed >= 24;
        } catch (Exception e) {
            return false;
        }
    }
}