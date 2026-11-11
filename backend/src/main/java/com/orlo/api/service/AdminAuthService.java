package com.orlo.api.service;

import com.orlo.api.dto.admin.LoginRequest;
import com.orlo.api.dto.admin.LoginResponse;

public interface AdminAuthService {

    /** clientIp sirve para limitar los intentos por IP. */
    LoginResponse login(LoginRequest request, String clientIp);
}
