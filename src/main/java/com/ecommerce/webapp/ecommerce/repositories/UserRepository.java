package com.ecommerce.webapp.ecommerce.repositories;

import com.ecommerce.webapp.ecommerce.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Integer> {

    public User findByEmail (String email);

    public List<User> findByRole (String role);

    public User findByResetToken (String token);

    public Boolean existsByEmail (String email);
}
