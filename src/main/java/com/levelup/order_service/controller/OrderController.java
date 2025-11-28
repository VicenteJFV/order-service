package com.levelup.order_service.controller;

import com.levelup.order_service.model.Order;
import com.levelup.order_service.model.OrderItem;
import com.levelup.order_service.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        if (orderRepository.existsById(id)) {
            orderRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        return orderRepository.findById(id)
                .map(order -> ResponseEntity.ok(order))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Order> updateOrder(@PathVariable Long id, @RequestBody Order orderDetails) {
        return orderRepository.findById(id)
                .map(existingOrder -> {
                    // Solo actualizamos los campos de envío/pago (lo que el usuario podría corregir)
                    existingOrder.setCustomerName(orderDetails.getCustomerName());
                    existingOrder.setCustomerPhone(orderDetails.getCustomerPhone());
                    existingOrder.setDeliveryAddress(orderDetails.getDeliveryAddress());
                    existingOrder.setPaymentMethod(orderDetails.getPaymentMethod());

                    // NOTA IMPORTANTE:
                    // NO actualizamos TotalAmount ni Items para preservar el monto original de la compra.

                    // Agregamos un marcador visual si viene en el body
                    if (orderDetails.getCustomerName().contains("(Editado)")) {
                        // Solo si el cliente envió la marca de actualización, la guardamos
                        existingOrder.setCustomerName(orderDetails.getCustomerName());
                    } else {
                        // Si el cliente no envió la marca, al menos actualizamos el nombre
                        existingOrder.setCustomerName(orderDetails.getCustomerName());
                    }


                    Order updatedOrder = orderRepository.save(existingOrder);
                    return ResponseEntity.ok(updatedOrder);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}