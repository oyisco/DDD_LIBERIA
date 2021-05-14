package org.fhi360.ddd.repositories;

import org.fhi360.ddd.domain.CommunityPharmacy;
import org.fhi360.ddd.domain.Facility;
import org.fhi360.ddd.domain.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    List<Patient> findByFacilityAndCommunityPharmacy(Facility facility, CommunityPharmacy communityPharmacy);

    Optional<Patient> findByHospitalNumAndFacility(String hospitalNum, Facility facility);

//    @Modifying
//    @Query(value = "UPDATE Patient set dateDiscontinued =:dateDiscontinued , reasonDiscontinued =:reasonDiscontinued where  id=:id")
//    void updatePatient(LocalDate dateDiscontinued, String reasonDiscontinued, Long id);

}
