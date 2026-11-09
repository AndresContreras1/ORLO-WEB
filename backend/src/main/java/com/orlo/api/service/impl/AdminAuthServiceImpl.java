package com.orlo.api.service.impl;

import com.orlo.api.dto.admin.LoginRequest;
import com.orlo.api.dto.admin.LoginResponse;
import com.orlo.api.security.JwtService;
import com.orlo.api.service.AdminAuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

@Service
public class AdminAuthServiceImpl implements AdminAuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AdminAuthServiceImpl(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    /** Si las credenciales no coinciden, el AuthenticationManager lanza una excepción que termina en 401. */
    @Override
    public LoginResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email().trim().toLowerCase(), request.password()));
        var roles = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        String token = jwtService.generate(auth.getName(), roles);
        return new LoginResponse(token, "Bearer", jwtService.expirationSeconds(), auth.getName());
    }
}
