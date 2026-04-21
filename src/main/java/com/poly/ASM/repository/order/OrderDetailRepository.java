package com.poly.ASM.repository.order;

import com.poly.ASM.entity.order.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.time.LocalDateTime;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {

    @Query("select d from OrderDetail d join fetch d.product where d.order.id = ?1")
    List<OrderDetail> findByOrderId(Long orderId);

    void deleteByOrderId(Long orderId);

    boolean existsByProductId(Integer productId);

    @Query("select d from OrderDetail d join fetch d.product where d.order.account.username = ?1")
    List<OrderDetail> findByOrderAccountUsername(String username);

    @Query("""
            select c.name as categoryName,
                   sum(d.price * d.quantity) as totalAmount,
                   sum(d.quantity) as totalQuantity,
                   max(d.price) as maxPrice,
                   min(d.price) as minPrice,
                   avg(d.price) as avgPrice
            from OrderDetail d
            join d.product p
            join p.category c
            group by c.name
            """)
    List<RevenueReport> getRevenueByCategory();

    @Query("""
            select d
            from OrderDetail d
            join fetch d.order o
            join fetch d.product p
            left join fetch p.category c
            where o.status in ('DELIVERED_SUCCESS', 'DONE')
            order by o.id asc, d.id asc
            """)
    List<OrderDetail> findDeliveredDetailsForRevenue();

    @Query("""
            select d
            from OrderDetail d
            join fetch d.order o
            join fetch d.product p
            left join fetch p.category c
            where o.status in ('DELIVERED_SUCCESS', 'DONE')
              and (:fromDate is null or o.createDate >= :fromDate)
              and (:toDate is null or o.createDate <= :toDate)
            order by o.id asc, d.id asc
            """)
    List<OrderDetail> findDeliveredDetailsForRevenueInRange(@Param("fromDate") LocalDateTime fromDate,
                                                            @Param("toDate") LocalDateTime toDate);
}
