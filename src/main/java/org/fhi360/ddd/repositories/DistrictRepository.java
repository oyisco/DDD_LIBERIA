package org.fhi360.ddd.repositories;

import java.util.List;
import org.fhi360.ddd.domain.District;
import org.fhi360.ddd.domain.State;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DistrictRepository extends JpaRepository<District, Long> {
  List<District> findByState(State paramState);
}
