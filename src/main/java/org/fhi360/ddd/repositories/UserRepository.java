package org.fhi360.ddd.repositories;

import java.util.Optional;

import org.fhi360.ddd.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsernameAndPasswordIgnoreCase(String paramString1, String paramString2);
}
