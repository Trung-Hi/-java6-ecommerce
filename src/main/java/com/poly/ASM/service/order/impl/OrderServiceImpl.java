package com.poly.ASM.service.order.impl;

import com.poly.ASM.entity.order.Order;
import com.poly.ASM.entity.order.OrderDetail;
import com.poly.ASM.entity.product.ProductSize;
import com.poly.ASM.repository.order.OrderRepository;
import com.poly.ASM.service.order.OrderDetailService;
import com.poly.ASM.service.order.OrderService;
import com.poly.ASM.service.product.ProductSizeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailService orderDetailService;
    private final ProductSizeService productSizeService;

    @Override
    public List<Order> findAll() {
        return orderRepository.findAllWithAccount();
    }

    @Override
    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    @Override
    public List<Order> findByAccountUsername(String username) {
        return orderRepository.findByAccountUsernameOrderByCreateDateDesc(username);
    }

    @Override
    public Optional<Order> findLatestPendingPaymentByUsername(String username) {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }
        return orderRepository.findFirstByAccountUsernameAndStatusOrderByCreateDateDesc(username, "PENDING_PAYMENT");
    }

    @Override
    public Order create(Order order) {
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order update(Order order) {
        if (order == null || order.getId() == null) {
            return orderRepository.save(order);
        }
        Optional<Order> existingOpt = orderRepository.findById(order.getId());
        String previousStatus = existingOpt.map(Order::getStatus).orElse(null);
        String nextStatus = order.getStatus();
        if (nextStatus != null && !nextStatus.equals(previousStatus)) {
            boolean hadReserved = hasReservedInventory(previousStatus);
            boolean reserveNow = isPlacedStatus(nextStatus);
            if (!hadReserved && reserveNow) {
                adjustInventory(order.getId(), -1);
            } else if (!"DELIVERY_FAILED".equals(previousStatus) && "DELIVERY_FAILED".equals(nextStatus) && hadReserved) {
                adjustInventory(order.getId(), 1);
            }
        }
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public void reserveInventoryForOrder(Long orderId) {
        adjustInventory(orderId, -1);
    }

    @Override
    public void deleteById(Long id) {
        orderRepository.deleteById(id);
    }

    private boolean isPlacedStatus(String status) {
        return "PLACED_UNPAID".equals(status) || "PLACED_PAID".equals(status);
    }

    private boolean hasReservedInventory(String status) {
        if (status == null) {
            return false;
        }
        return "PLACED_UNPAID".equals(status)
                || "PLACED_PAID".equals(status)
                || "SHIPPING_UNPAID".equals(status)
                || "SHIPPING_PAID".equals(status)
                || "DELIVERED_SUCCESS".equals(status)
                || "DONE".equals(status)
                || "REFUND_REQUEST".equals(status);
    }

    private void adjustInventory(Long orderId, int direction) {
        if (orderId == null) {
            return;
        }
        List<OrderDetail> details = orderDetailService.findByOrderId(orderId);
        for (OrderDetail detail : details) {
            Integer productId = detail.getProduct() != null ? detail.getProduct().getId() : null;
            Integer sizeId = detail.getSizeId();
            Integer qty = detail.getQuantity();
            if (productId == null || sizeId == null || qty == null) {
                continue;
            }
            Optional<ProductSize> productSizeOpt = productSizeService.findByProductIdAndSizeId(productId, sizeId);
            if (productSizeOpt.isEmpty() || productSizeOpt.get().getQuantity() == null) {
                continue;
            }
            ProductSize productSize = productSizeOpt.get();
            int current = productSize.getQuantity();
            int next = current + (direction * qty);
            if (next < 0) {
                next = 0;
            }
            productSize.setQuantity(next);
            productSizeService.save(productSize);
        }
    }
}
