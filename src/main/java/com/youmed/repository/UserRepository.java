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

    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u WHERE " +
           "(:#{#role == null} = true OR u.role = :role) AND " +
           "(:#{#keyword == null} = true OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.phone) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<User> searchUsers(@org.springframework.data.repository.query.Param("keyword") String keyword, @org.springframework.data.repository.query.Param("role") Role role, Pageable pageable);
}