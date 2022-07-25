package org.fhi360.ddd.repositories;

import java.util.List;
import java.util.Optional;
import org.fhi360.ddd.domain.CommunityPharmacy;
import org.fhi360.ddd.domain.Facility;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityPharmacyRepository extends JpaRepository<CommunityPharmacy, Long> {
  Optional<CommunityPharmacy> findByPinIgnoreCase(String paramString);

  Optional<CommunityPharmacy> findByEmail(String paramString);

  List<CommunityPharmacy> findByFacility(Facility paramFacility);
}

