package com.mma.gestion.service;

import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.mma.gestion.entity.User;
import com.mma.gestion.repository.UserRepository;
import com.mma.gestion.security.UserDetailsImpl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        return new UserDetailsImpl(
            user.getId(),
            user.getUsername(),
            user.getPassword(),
            // .name() convierte ROLE_ADMIN (Enum) a "ROLE_ADMIN" (String)
            Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name()))
        );
    }
}
