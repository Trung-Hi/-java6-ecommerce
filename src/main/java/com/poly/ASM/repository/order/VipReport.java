package com.poly.ASM.repository.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface VipReport {

    String getUsername();

    String getFullname();

    String getPhoto();

    String getEmail();

    String getAddress();

    String getPhone();

    BigDecimal getTotalAmount();

    LocalDateTime getFirstOrderDate();

    LocalDateTime getLastOrderDate();
}
