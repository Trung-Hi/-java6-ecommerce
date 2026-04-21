package com.poly.ASM.controller.advice;

import com.poly.ASM.entity.user.Account;
import com.poly.ASM.entity.product.Category;
import com.poly.ASM.service.auth.AuthService;
import com.poly.ASM.entity.notification.Notification;
import com.poly.ASM.service.cart.CartService;
import com.poly.ASM.service.notification.NotificationService;
import com.poly.ASM.service.product.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalAdvice {

    private final CategoryService categoryService;
    private final AuthService authService;
    private final NotificationService notificationService;
    private final CartService cartService;

    @ModelAttribute("navCategories")
    public List<Category> navCategories() {
        return categoryService.findAll();
    }

    @ModelAttribute("isAdmin")
    public boolean isAdmin() {
        return authService.hasRole("ADMIN");
    }

    @ModelAttribute("isAuthenticated")
    public boolean isAuthenticated() {
        return authService.isAuthenticated();
    }

    @ModelAttribute("currentUser")
    public Account currentUser() {
        return authService.getUser();
    }

    @ModelAttribute("unreadNotificationCount")
    public long unreadNotificationCount() {
        Account user = authService.getUser();
        if (user == null) {
            return 0;
        }
        return notificationService.countUnread(user.getUsername());
    }

    @ModelAttribute("latestNotifications")
    public List<Notification> latestNotifications() {
        Account user = authService.getUser();
        if (user == null) {
            return java.util.List.of();
        }
        return notificationService.getLatest(user.getUsername(), 100);
    }

    @ModelAttribute("cartDistinctCount")
    public long cartDistinctCount() {
        return cartService.getDistinctProductCount();
    }

    @ModelAttribute("cartProductIds")
    public java.util.Set<Integer> cartProductIds() {
        return cartService.getProductIdsInCart();
    }
}
