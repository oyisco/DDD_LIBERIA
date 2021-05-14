package org.fhi360.ddd.repositories;

import org.fhi360.ddd.domain.CommunityPharmacy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CommunityPharmacyRepository extends JpaRepository<CommunityPharmacy, Long> {
    Optional<CommunityPharmacy> findByPinIgnoreCase(String pin);
    Optional<CommunityPharmacy> findByEmail(String email);

}
