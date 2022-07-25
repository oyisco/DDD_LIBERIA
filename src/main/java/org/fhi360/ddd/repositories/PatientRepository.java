package org.fhi360.ddd.repositories;

import java.util.List;
import java.util.Optional;

import org.fhi360.ddd.domain.CommunityPharmacy;
import org.fhi360.ddd.domain.Facility;
import org.fhi360.ddd.domain.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    List<Patient> findByFacilityAndCommunityPharmacyAndArchived(Facility paramFacility, CommunityPharmacy paramCommunityPharmacy, Boolean paramBoolean);

    List<Patient> findByCommunityPharmacyAndArchived(CommunityPharmacy paramCommunityPharmacy, Boolean paramBoolean);

    Optional<Patient> findByUniqueIdAndFacility(String paramString, Facility paramFacility);



    List<Patient> findByFacility(Facility paramFacility);

    Patient findByUuid(String paramString);

    List<Patient> findByArchived(Boolean paramBoolean);


}
