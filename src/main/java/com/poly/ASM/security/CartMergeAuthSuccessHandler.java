package com.poly.ASM.security;

import com.poly.ASM.service.cart.CartService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CartMergeAuthSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final CartService cartService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws ServletException, IOException {
        if (authentication != null && authentication.getName() != null) {
            cartService.mergeSessionCartToUserCart(authentication.getName());
        }
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
