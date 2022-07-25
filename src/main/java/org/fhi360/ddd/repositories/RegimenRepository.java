package org.fhi360.ddd.repositories;

import org.fhi360.ddd.domain.Regimen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegimenRepository extends JpaRepository<Regimen, Long> {

    Regimen findByName(String name);

    List<Regimen> findByNameOrderByIdDesc(String name);


}

