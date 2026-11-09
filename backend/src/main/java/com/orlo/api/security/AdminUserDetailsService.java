package com.orlo.api.security;

import com.orlo.api.entities.AdminUser;
import com.orlo.api.repository.AdminUserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Carga las cuentas del panel para el login con correo y contraseña. */
@Service
public class AdminUserDetailsService implements UserDetailsService {

    private final AdminUserRepository adminUserRepository;

    public AdminUserDetailsService(AdminUserRepository adminUserRepository) {
        this.adminUserRepository = adminUserRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) {
        AdminUser admin = adminUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Credenciales inválidas"));
        var authorities = admin.getRoles().stream().map(r -> new SimpleGrantedAuthority(r.getName())).toList();
        return new User(admin.getEmail(), admin.getPassword(), admin.isEnabled(), true, true, true, authorities);
    }
}
