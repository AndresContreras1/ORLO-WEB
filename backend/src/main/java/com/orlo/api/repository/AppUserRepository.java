package com.orlo.api.repository;

import com.orlo.api.entities.AppUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, String> {

    long countByPremiumTrue();

    Page<AppUser> findByEmailContainingIgnoreCase(String email, Pageable pageable);
}
