package com.poly.ASM.repository.order;

import com.poly.ASM.entity.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("select o from Order o join fetch o.account order by o.createDate desc")
    List<Order> findAllWithAccount();

    List<Order> findByAccountUsernameOrderByCreateDateDesc(String username);

    Optional<Order> findFirstByAccountUsernameAndStatusOrderByCreateDateDesc(String username, String status);

    @Query("""
            select a.username as username,
                   a.fullname as fullname,
                   a.photo as photo,
                   a.email as email,
                   a.address as address,
                   a.phone as phone,
                   sum(d.price * d.quantity) as totalAmount,
                   min(o.createDate) as firstOrderDate,
                   max(o.createDate) as lastOrderDate
            from Order o
            join o.account a
            join o.orderDetails d
            where a.isDelete = false
            group by a.username, a.fullname, a.photo, a.email, a.address, a.phone
            order by sum(d.price * d.quantity) desc
            """)
    List<VipReport> getVipCustomers(Pageable pageable);
}
