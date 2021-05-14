package org.fhi360.ddd.repositories;

import org.fhi360.ddd.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long>{
}
