package com.guilhermebraga.agendamento_api.controller;

import com.guilhermebraga.agendamento_api.dto.request.LoginRequest;
import com.guilhermebraga.agendamento_api.dto.response.LoginResponse;
import com.guilhermebraga.agendamento_api.security.JwtTokenProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getSenha())
        );

        String token = jwtTokenProvider.generateToken(authentication.getName());

        return ResponseEntity.ok(LoginResponse.builder()
            .token(token)
            .tipo("Bearer")
            .expiracao(LocalDateTime.now().plusHours(24))
            .build());
    }
}
