package com.mma.gestion.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mma.gestion.dto.AuthResponseDTO;
import com.mma.gestion.dto.LoginRequestDTO;
import com.mma.gestion.dto.UserRegistrationDTO;
import com.mma.gestion.entity.User;
import com.mma.gestion.repository.UserRepository;
import com.mma.gestion.security.JwtUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    public String register(UserRegistrationDTO dto) {
        // Validar existencia
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new RuntimeException("El nombre de usuario ya existe");
        }

        // Crear usuario
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(dto.getRole()); 

        userRepository.save(user);
        
        return "Usuario registrado exitosamente";
    }

    public AuthResponseDTO login(LoginRequestDTO request) {
        // Autenticar con Spring Security (esto valida la pass automáticamente)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // Generamos el token usando tu JwtUtils
        String token = jwtUtils.generateJwtToken(authentication);

        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        return AuthResponseDTO.builder()
            .token(token)
            .username(user.getUsername())
            .role(user.getRole().name()) // Esto devuelve "ADMIN", "USER", etc.
            .build();
    }
}
