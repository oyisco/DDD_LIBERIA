package org.fhi360.ddd.utils;

import liquibase.pro.packaged.F;
import liquibase.pro.packaged.L;
import lombok.RequiredArgsConstructor;
import org.fhi360.ddd.domain.*;
import org.fhi360.ddd.repositories.CommunityPharmacyRepository;
import org.fhi360.ddd.repositories.FacilityRepository;
import org.fhi360.ddd.repositories.PatientRepository;
import org.fhi360.ddd.repositories.RegimenRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class DataLoader {
    private final FacilityRepository facilityRepository;
    private final CommunityPharmacyRepository communityPharmacyRepository;
    private final RegimenRepository regimenRepository;
    private final PatientRepository patientRepository;
    private final JdbcTemplate jdbcTemplate;

    public void saveFac(String name, Long stateId, Long lgaId) {

        System.out.println("SUCCESS FACILITY");
        Facility facility = new Facility();
        System.out.println("SUCCESS FACILITY");
        // facility.setId(id);
        System.out.println("SUCCESS FACILITY 2");
        facility.setName(name);
        System.out.println("SUCCESS FACILITY 3");
        State state1 = new State();
        System.out.println("SUCCESS FACILITY 5");
        state1.setId(stateId);
        System.out.println("SUCCESS FACILITY 4");
        facility.setState(state1);
        System.out.println("SUCCESS FACILITY 6");
        District district1 = new District();
        System.out.println("SUCCESS FACILITY 7");
        district1.setId(lgaId);
        System.out.println("SUCCESS FACILITY 8");
        facility.setDistrict(district1);
        System.out.println("SUCCESS FACILITY 9");
        facilityRepository.save(facility);
        System.out.println("SUCCESS FACILITY");

    }

//    @PostConstruct
//    public void setDataStyl1() {
//       // jdbcTemplate.query("SELECT");
//        //jdbcTemplate.execute("SET datestyle = dmy");
////06-17-2022
//    //    System.out.println("DONE");
//        //14-06-2021
//    }


//    @PostConstruct
//    public void setDataStyl2() {
//        //jdbcTemplate.execute("SET datestyle = dmy");
//        jdbcTemplate.execute("SET datestyle = mdy");
//        System.out.println("DONE");
//    }


//    @PostConstruct
//    public void setDataStyle() {
//
//        jdbcTemplate.execute("ALTER TABLE public.patient ALTER COLUMN date_started TYPE date USING (date_started::date)");
//
//        jdbcTemplate.execute("ALTER TABLE public.patient ALTER COLUMN date_last_viral_load TYPE date USING (date_last_viral_load::date)");
//
//        jdbcTemplate.execute("ALTER TABLE public.patient ALTER COLUMN viral_load_due_date TYPE date USING (viral_load_due_date::date)");
//
//
//        jdbcTemplate.execute("ALTER TABLE public.patient ALTER COLUMN date_last_refill TYPE date USING (date_last_refill::date)");
//
//
//        jdbcTemplate.execute("ALTER TABLE public.patient ALTER COLUMN date_next_refill TYPE date USING (date_next_refill::date)");
//
//        jdbcTemplate.execute("ALTER TABLE public.patient ALTER COLUMN date_last_clinic TYPE date USING (date_last_clinic::date)");
//
//        jdbcTemplate.execute("ALTER TABLE public.patient ALTER COLUMN date_next_clinic TYPE date USING (date_next_clinic::date)");
//
//
//        System.out.println("oyisco");
//
//    }

    public void saveRegimen() {
        //  id,name,regimen_type_id
        Regimen regimen = this.regimenRepository.findByName("TDF 300mg +3TC 300mg +DTG 50mg");
        if (regimen != null) {

        } else {


            jdbcTemplate.update("INSERT INTO regimen (id,name, regimen_type_id) VALUES (5000,'TDF 300mg +3TC 300mg +DTG 50mg', 1)");

        }

    }

    public void savePharm(Long id, String name, Long facility_id, String address, String phone, String email, String pin) {
        CommunityPharmacy pharmacy = new CommunityPharmacy();
        Optional<CommunityPharmacy> communityPharmacy1 = this.communityPharmacyRepository.findByEmail(email);
        if (communityPharmacy1.isPresent()) {

        } else {
            pharmacy.setId(id);
            pharmacy.setName(name);
            Facility facility = new Facility();
            facility.setId(facility_id);
            pharmacy.setFacility(facility);
            pharmacy.setAddress(address);
            pharmacy.setPhone(phone);
            pharmacy.setEmail(email);
            pharmacy.setPin(pin);
            this.communityPharmacyRepository.save(pharmacy);
            System.out.println("SUCCESS PHARMACY");
        }
    }

}
