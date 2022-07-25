package org.fhi360.ddd.repositories;

import java.util.List;
import java.util.Optional;
import org.fhi360.ddd.domain.ARV;
import org.fhi360.ddd.domain.Facility;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ARVRepository extends JpaRepository<ARV, Long> {
  Optional<ARV> findByUuidAndFacility(String paramString, Facility paramFacility);

    List<ARV> findByArchived(Boolean paramBoolean);
}
