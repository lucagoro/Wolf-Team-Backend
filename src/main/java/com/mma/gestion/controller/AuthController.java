package com.mma.gestion.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mma.gestion.dto.AuthResponseDTO;
import com.mma.gestion.dto.LoginRequestDTO;
import com.mma.gestion.dto.UserRegistrationDTO;
import com.mma.gestion.service.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginRequestDTO loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody UserRegistrationDTO dto) {
        return ResponseEntity.ok(authService.register(dto));
    }
}
