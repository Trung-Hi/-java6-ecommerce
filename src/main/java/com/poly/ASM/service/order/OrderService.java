package com.poly.ASM.service.order;

import com.poly.ASM.entity.order.Order;

import java.util.List;
import java.util.Optional;

public interface OrderService {

    List<Order> findAll();

    Optional<Order> findById(Long id);

    List<Order> findByAccountUsername(String username);

    Optional<Order> findLatestPendingPaymentByUsername(String username);

    Order create(Order order);

    Order update(Order order);

    void reserveInventoryForOrder(Long orderId);

    void deleteById(Long id);
}
