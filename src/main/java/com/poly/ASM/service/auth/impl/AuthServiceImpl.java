package com.poly.ASM.service.auth.impl;

import com.poly.ASM.entity.user.Account;
import com.poly.ASM.repository.user.AuthorityRepository;
import com.poly.ASM.service.auth.AuthService;
import com.poly.ASM.service.user.AccountService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final HttpServletRequest request;
    private final AccountService accountService;
    private final AuthorityRepository authorityRepository;

    @Override
    public boolean login(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return false;
        }
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return authentication.isAuthenticated();
        } catch (AuthenticationException e) {
            return false;
        }
    }

    @Override
    public void logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        new SecurityContextLogoutHandler().logout(request, null, authentication);
        SecurityContextHolder.clearContext();
    }

    @Override
    public Account getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        if (authentication.getPrincipal() instanceof OAuth2User oAuth2User) {
            String email = toSafeString(oAuth2User.getAttribute("email"));
            if (!email.isBlank()) {
                return accountService.findByEmail(email).orElse(null);
            }
        }
        String username = authentication.getName();
        if (username == null || "anonymousUser".equals(username)) {
            return null;
        }
        return accountService.findByUsername(username).orElse(null);
    }

    @Override
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName());
    }

    @Override
    public boolean hasRole(String role) {
        if (role == null || role.isBlank()) {
            return false;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        Account account = getUser();
        if (account == null) {
            return false;
        }
        return authorityRepository.existsByAccountUsernameAndRoleId(account.getUsername(), role);
    }

    private String toSafeString(Object value) {
        if (value == null) {
            return "";
        }
        return value.toString().trim();
    }
}
