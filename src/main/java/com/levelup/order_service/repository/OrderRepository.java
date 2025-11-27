package com.levelup.order_service.repository;

import com.levelup.order_service.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    // Al extender de JpaRepository, ya tenemos listos los métodos
    // save(), findAll(), findById(), etc. sin escribir nada más.
}