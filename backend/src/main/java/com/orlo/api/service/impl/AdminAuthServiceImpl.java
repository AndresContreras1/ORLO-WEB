package com.orlo.api.service.impl;

import com.orlo.api.dto.admin.LoginRequest;
import com.orlo.api.dto.admin.LoginResponse;
import com.orlo.api.security.JwtService;
import com.orlo.api.service.AdminAuthService;
import com.orlo.api.service.LoginAttemptService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

@Service
public class AdminAuthServiceImpl implements AdminAuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final LoginAttemptService loginAttemptService;

    public AdminAuthServiceImpl(AuthenticationManager authenticationManager, JwtService jwtService,
                                LoginAttemptService loginAttemptService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.loginAttemptService = loginAttemptService;
    }

    /**
     * Primero revisa el límite de intentos (429). Si las credenciales no coinciden, el
     * AuthenticationManager lanza una excepción que termina en 401 y se cuenta el fallo.
     */
    @Override
    public LoginResponse login(LoginRequest request, String clientIp) {
        String email = request.email().trim().toLowerCase();
        loginAttemptService.checkAllowed(email, clientIp);
        Authentication auth;
        try {
            auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password()));
        } catch (AuthenticationException e) {
            loginAttemptService.recordFailure(email, clientIp);
            throw e;
        }
        loginAttemptService.recordSuccess(email);
        var roles = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        String token = jwtService.generate(auth.getName(), roles);
        return new LoginResponse(token, "Bearer", jwtService.expirationSeconds(), auth.getName());
    }
}
