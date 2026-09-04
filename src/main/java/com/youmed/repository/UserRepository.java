package com.youmed.repository;

import com.youmed.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.youmed.enums.Role;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    
    Page<User> findByRoleAndFullNameContainingIgnoreCase(Role role, String fullName, Pageable pageable);
    
    Page<User> findByRole(Role role, Pageable pageable);

    boolean existsByEmail(String email);
}