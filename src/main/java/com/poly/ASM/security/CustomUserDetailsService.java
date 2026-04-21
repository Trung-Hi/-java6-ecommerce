package com.poly.ASM.security;

import com.poly.ASM.entity.user.Account;
import com.poly.ASM.entity.user.Authority;
import com.poly.ASM.repository.user.AccountRepository;
import com.poly.ASM.repository.user.AuthorityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AccountRepository accountRepository;
    private final AuthorityRepository authorityRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findByUsernameAndIsDeleteFalse(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<GrantedAuthority> grantedAuthorities = authorityRepository.findByAccountUsernameWithRole(username).stream()
                .map(Authority::getRole)
                .filter(role -> role != null && role.getId() != null && !role.getId().isBlank())
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getId()))
                .map(GrantedAuthority.class::cast)
                .toList();

        return User.withUsername(account.getUsername())
                .password(account.getPassword())
                .authorities(grantedAuthorities)
                .disabled(account.getActivated() != null && !account.getActivated())
                .build();
    }
}
