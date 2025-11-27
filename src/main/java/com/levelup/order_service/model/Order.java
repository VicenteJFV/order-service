package com.levelup.order_service.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- Datos del Cliente ---
    private String customerName;
    private String customerPhone;     // Nuevo
    private String deliveryAddress;   // Nuevo
    private String paymentMethod;     // Nuevo

    // --- Datos del Sistema ---
    private String status;
    private LocalDateTime createdAt;
    private BigDecimal totalAmount;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    // --- Métodos IMPORTANTES (No los borres) ---

    // 1. Vincula los hijos con el padre
    public void addItems(List<OrderItem> newItems) {
        for (OrderItem item : newItems) {
            item.setOrder(this);
            this.items.add(item);
        }
    }

    // 2. LA MAGIA: Rellena fecha y estado automáticamente antes de guardar
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now(); // Pone la fecha/hora actual
        if (this.status == null) {
            this.status = "CREATED";      // Pone el estado por defecto
        }
    }
}