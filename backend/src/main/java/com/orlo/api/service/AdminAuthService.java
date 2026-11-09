package com.orlo.api.service;

import com.orlo.api.dto.admin.LoginRequest;
import com.orlo.api.dto.admin.LoginResponse;

public interface AdminAuthService {

    LoginResponse login(LoginRequest request);
}
