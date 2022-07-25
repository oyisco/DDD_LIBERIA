package org.fhi360.ddd.web;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.fhi360.ddd.domain.*;
import org.fhi360.ddd.dto.*;
import org.fhi360.ddd.repositories.*;
import org.fhi360.ddd.utils.EmailSender;
import org.fhi360.ddd.utils.ResourceException;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.*;


@RestController
@RequestMapping("/api/ddd/")
@RequiredArgsConstructor
public class DDDResource {
    private final ARVRepository arvRepository;
    private final FacilityRepository facilityRepository;
    private final PatientRepository patientRepository;
    private final CommunityPharmacyRepository communityPharmacyRepository;
    private final RegimenRepository regimenRepository;
    private final DeviceConfigRepository deviceConfigRepository;
    private final StateRepository stateRepository;
    private final DistrictRepository districtRepository;
    private final EmailSender emailSender;
    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;
    private final InventoryRepository inventoryRepository;
    private final DrugRepository drugRepository;
    DateTimeFormatter df = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    @PostMapping("mobile/pharmacy")
    private ResponseEntity<Response> saveOrUpdatePharmacy(@RequestBody CommunityPharmacy pharmacy) {
        Response response = new Response();
        try {
            Optional<CommunityPharmacy> communityPharmacy1 = this.communityPharmacyRepository.findByEmail(pharmacy.getEmail());
            if (communityPharmacy1.isPresent()) {
                response.setMessage("Outlet with these credentials already exist");
                return ResponseEntity.ok(response);
            }
            long count = this.communityPharmacyRepository.count();
            count++;
            String activationCode = "LIB00" + count;
            pharmacy.setPin(activationCode);
            CommunityPharmacy communityPharmacy = this.communityPharmacyRepository.save(pharmacy);
            //  String message = this.emailSender.activation(communityPharmacy.getName(), communityPharmacy.getUsername(), activationCode);
            //this.emailSender.sendMail(communityPharmacy.getEmail(), "DDD Activation", message);
            response.setPharmacy(communityPharmacy);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("mobile/sync/patient")
    private ResponseEntity<Response> syncPatient(@RequestBody List<Patient> patientDto) {
        Response response = new Response();
        patientDto.forEach(patient -> {
            Facility facility = this.facilityRepository.getOne(Objects.<Long>requireNonNull(patient.getFacility().getId()));
            Optional<Patient> patient1 = this.patientRepository.findByUniqueIdAndFacility(patient.getHospitalNum(), facility);
            if (patient1.isPresent()) {
                patient = patient1.get();
                CommunityPharmacy communityPharmacy = this.communityPharmacyRepository.getOne(patient.getCommunityPharmacy().getId());
                patient.setCommunityPharmacy(communityPharmacy);
                this.patientRepository.save(patient);
            } else {
                CommunityPharmacy communityPharmacy = this.communityPharmacyRepository.getOne(patient.getPharmacyId());
                patient.setCommunityPharmacy(communityPharmacy);
                this.patientRepository.save(patient);
            }
        });
        response.setMessage("patient success");
        return ResponseEntity.ok(response);
    }


    @PostMapping("mobile/save/patient")
    private ResponseEntity<Respone2> savePatient(@RequestBody PatientDto patient2) {
        Respone2 response = new Respone2();
        Patient patient = new Patient();
        Facility facility1 = new Facility();
        facility1.setId(patient2.getFacility().getId());
        Optional<Patient> patient1 = this.patientRepository.findByUniqueIdAndFacility(patient2.getUniqueId(), facility1);
        if (patient1.isPresent()) {
            response.setMessage("Patient already exist");
            return ResponseEntity.ok(response);
        }
        CommunityPharmacy communityPharmacy = this.communityPharmacyRepository.getOne(patient2.getPharmacyId());
        patient.setCommunityPharmacy(communityPharmacy);
        patient.setHospitalNum(patient2.getHospitalNum());
        patient.setFacility(facility1);
        patient.setUniqueId(patient2.getUniqueId());
        patient.setSurname(patient2.getSurname());
        patient.setOtherNames(patient2.getOtherNames());
        patient.setGender(patient2.getGender());
        patient.setDateBirth(LocalDate.parse(patient2.getDateBirth(), df));
        patient.setAddress(patient2.getAddress());
        patient.setPhone(patient2.getPhone());
        patient.setDateStarted(LocalDate.parse(patient2.getDateStarted(), df));
        patient.setLastClinicStage(patient2.getLastClinicStage());
        patient.setLastViralLoad(patient2.getLastViralLoad());
        String dateLastViralLoad1 = patient2.getDateLastViralLoad();
        patient.setDateLastViralLoad(LocalDate.parse(dateLastViralLoad1, df));
        String viralLoadDueDate1 = patient2.getViralLoadDueDate();
        patient.setViralLoadDueDate(LocalDate.parse(viralLoadDueDate1, df));
        patient.setViralLoadType(patient2.getViralLoadType());
        String dateLastClinic1 = patient2.getDateLastClinic();
        patient.setDateLastClinic(LocalDate.parse(dateLastClinic1, df));
        String dateNextClinic1 = patient2.getDateNextClinic();
        patient.setDateNextClinic(LocalDate.parse(dateNextClinic1, df));
        String dateLastRefill1 = patient2.getDateLastRefill();
        patient.setDateLastRefill(LocalDate.parse(dateLastRefill1, df));
        String dateNextRefill1 = patient2.getDateNextRefill();
        patient.setDateNextRefill(LocalDate.parse(dateNextRefill1, df));
        Patient patient3 = this.patientRepository.save(patient);


        PatientDto patient12 = new PatientDto();
        patient12.setId(patient3.getId());
        patient12.setHospitalNum(patient3.getHospitalNum());
        patient12.setFacility(patient3.getFacility());
        patient12.setUniqueId(patient3.getUniqueId());
        patient12.setSurname(patient3.getSurname());
        patient12.setOtherNames(patient3.getOtherNames());
        patient12.setGender(patient3.getGender());
        String dob = patient3.getDateBirth().format(df);
        patient12.setDateBirth(dob);
        patient12.setAddress(patient3.getAddress());
        patient12.setPhone(patient3.getPhone());
        patient12.setDateStarted(patient3.getDateStarted().format(df));
        patient12.setLastClinicStage(patient3.getLastClinicStage());
        patient12.setLastViralLoad(patient3.getLastViralLoad());
        patient12.setDateLastViralLoad(patient3.getDateLastViralLoad().format(df));
        patient12.setViralLoadDueDate(patient3.getViralLoadDueDate().format(df));
        patient12.setViralLoadType(patient3.getViralLoadType());
        patient12.setDateLastClinic(patient3.getDateLastClinic().format(df));
        patient12.setDateNextClinic(patient3.getDateNextClinic().format(df));
        patient12.setDateLastRefill(patient3.getDateLastRefill().format(df));
        patient12.setDateNextRefill(patient3.getDateNextRefill().format(df));
        patient12.setPharmacyId(patient3.getCommunityPharmacy().getId());
        patient12.setUuid(patient3.getUuid());
        patient12.setPharmacyId(communityPharmacy.getId());
        response.setMessage("Patient save successfully");
        response.setPatient(patient12);
        return ResponseEntity.ok(response);

    }

    @PostMapping("mobile/update/patient")
    private ResponseEntity<Respone2> updatePatient(@RequestBody PatientDto patient2) {
        Respone2 response = new Respone2();
        System.out.println("Oyisco done 1");
        Patient patient = new Patient();
        System.out.println("Oyisco done 2");
        Facility facility1 = new Facility();
        facility1.setId(patient2.getFacility().getId());
        System.out.println("Oyisco done 3");
        Patient patient1 = this.patientRepository.getOne(patient2.getId());
        System.out.println("Oyisco done 4");
        if (patient1 != null) {
            System.out.println("Oyisco done 5");
            CommunityPharmacy communityPharmacy = this.communityPharmacyRepository.getOne(patient2.getPharmacyId());
            System.out.println("Oyisco done 6");
            patient.setId(patient1.getId());
            System.out.println("Oyisco done 7");
            patient.setCommunityPharmacy(communityPharmacy);
            System.out.println("Oyisco done 8");
            patient.setHospitalNum(patient2.getHospitalNum());
            System.out.println("Oyisco done 9");
            patient.setFacility(facility1);
            System.out.println("Oyisco done 10");
            patient.setUniqueId(patient2.getUniqueId());
            System.out.println("Oyisco done 11");
            patient.setSurname(patient2.getSurname());
            System.out.println("Oyisco done 12");
            patient.setOtherNames(patient2.getOtherNames());
            System.out.println("Oyisco done 13");
            patient.setGender(patient2.getGender());
            System.out.println("Oyisco done 14");
            DateTimeFormatter df = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            patient.setDateBirth(LocalDate.parse(patient2.getDateBirth(), df));
            patient.setAddress(patient2.getAddress());
            patient.setPhone(patient2.getPhone());
            System.out.println("Oyisco done 16");
            patient.setDateStarted(LocalDate.parse(patient2.getDateStarted(), df));
            System.out.println("Oyisco done 17");
            patient.setLastClinicStage(patient2.getLastClinicStage());
            System.out.println("Oyisco done 11");
            patient.setLastViralLoad(patient2.getLastViralLoad());
            System.out.println("Oyisco done 73");
            String dateLastViralLoad1 = patient2.getDateLastViralLoad();
            System.out.println("Oyisco done 72");
            patient.setDateLastViralLoad(LocalDate.parse(dateLastViralLoad1, df));
            System.out.println("Oyisco done 73");
            String viralLoadDueDate1 = patient2.getViralLoadDueDate();
            System.out.println("Oyisco done 46");
            patient.setViralLoadDueDate(LocalDate.parse(viralLoadDueDate1, df));
            System.out.println("Oyisco done 4040");
            patient.setViralLoadType(patient2.getViralLoadType());
            System.out.println("Oyisco done 44664");
            String dateLastClinic1 = patient2.getDateLastClinic();
            System.out.println("Oyisco done 66886");
            patient.setDateLastClinic(LocalDate.parse(dateLastClinic1, df));
            System.out.println("Oyisco done 8989");
            String dateNextClinic1 = patient2.getDateNextClinic();
            System.out.println("Oyisco done 0001");
            patient.setDateNextClinic(LocalDate.parse(dateNextClinic1, df));
            System.out.println("Oyisco done 199");
            String dateLastRefill1 = patient2.getDateLastRefill();
            System.out.println("Oyisco done 7000");
            patient.setDateLastRefill(LocalDate.parse(dateLastRefill1, df));
            String dateNextRefill1 = patient2.getDateNextRefill();
            System.out.println("Oyisco done 71111");
            patient.setDateNextRefill(LocalDate.parse(dateNextRefill1, df));
            System.out.println("Oyisco done 74444");
            patient.setUuid(patient1.getUuid());
            System.out.println("Oyisco done");
            this.patientRepository.save(patient);
            response.setPatient(patient2);
            response.setMessage("Patient updated successfully");
        }

        return ResponseEntity.ok(response);


    }


    @PostMapping("mobile/sync/arv")
    private ResponseEntity<Response> syncARV(@RequestBody List<ARVDto> arvDtos) {
        Response response = new Response();
        arvDtos.forEach(arvs -> {
            Facility facility = this.facilityRepository.getOne(arvs.getFacilityId());
            Patient patient = this.patientRepository.getOne(arvs.getPatient().getId());
            Optional<ARV> checkIfExist = this.arvRepository.findByUuidAndFacility(arvs.getUuid(), facility);
            if (checkIfExist.isPresent()) {
                ARV arv = checkIfExist.get();
                arv.setPatient(patient);
                arv.setFacility(facility);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                arv.setDateVisit(LocalDate.parse(arvs.getDateVisit(), formatter));
                arv.setDateNextRefill(LocalDate.parse(arvs.getDateNextRefill(), formatter));
                arv.setBodyWeight(arvs.getBodyWeight());
                arv.setHeight(arvs.getHeight());
                arv.setBp(arvs.getBp());
                arv.setBmi(arvs.getBmi());
                arv.setBmiCategory(arvs.getBmiCategory());
                if (arvs.getId() != null) {
                    arv.setItp(Boolean.TRUE);
                } else {
                    arv.setItp(Boolean.FALSE);
                }
                if (arvs.getHaveYouBeenCoughing() != null) {
                    arv.setCoughing(Boolean.TRUE);
                } else {
                    arv.setCoughing(Boolean.FALSE);
                }
                if (arvs.getDoYouHaveFever() != null) {
                    arv.setFever(Boolean.TRUE);
                } else {
                    arv.setFever(Boolean.FALSE);
                }
                if (arvs.getAreYouLosingWeight() != null) {
                    arv.setWeightLoss(Boolean.TRUE);
                } else {
                    arv.setWeightLoss(Boolean.FALSE);
                }
                if (arvs.getAreYouHavingSweet() != null) {
                    arv.setSweating(Boolean.TRUE);
                } else {
                    arv.setSweating(Boolean.FALSE);
                }
                if (arvs.getDoYouHaveSwellingNeck() != null) {
                    arv.setSwellingNeck(Boolean.TRUE);
                } else {
                    arv.setSwellingNeck(Boolean.FALSE);
                }
                if (arvs.getTbReferred() != null) {
                    arv.setTbReferred(Boolean.TRUE);
                } else {
                    arv.setTbReferred(Boolean.FALSE);
                }
                if (arvs.getEligibleIpt() != null) {
                    arv.setIptEligible(Boolean.TRUE);
                } else {
                    arv.setIptEligible(Boolean.FALSE);
                }
                Regimen regimen1 = this.regimenRepository.getOne(arvs.getRegimen1());
                arv.setRegimen1(regimen1);
                arv.setDuration1(arvs.getDuration1());
                if (!StringUtils.isBlank(arvs.getDispensed1())) {
                    arv.setQuantityDispensed1(Double.valueOf(arvs.getDispensed1()));
                }
                arv.setQuantityPrescribed1(Double.valueOf(arvs.getPrescribed1()));
                Regimen regimen2 = this.regimenRepository.getOne(arvs.getRegimen2());
                arv.setRegimen2(regimen2);
                arv.setDuration2(arvs.getDuration2());
                if (!StringUtils.isBlank(arvs.getPrescribed2()) || !StringUtils.isBlank(arvs.getPrescribed2())) {
                    arv.setQuantityPrescribed2(Double.valueOf(arvs.getPrescribed2()));
                }
                if (!StringUtils.isBlank(arvs.getDispensed2())) {
                    arv.setQuantityDispensed2(Double.valueOf(arvs.getDispensed2()));
                }
                Regimen regimen3 = this.regimenRepository.getOne(arvs.getRegimen3());
                arv.setRegimen3(regimen3);
                arv.setDuration3(arvs.getDuration3());
                if (!StringUtils.isBlank(arvs.getPrescribed3()) || !StringUtils.isBlank(arvs.getPrescribed3())) {
                    arv.setQuantityPrescribed3(Double.valueOf(arvs.getPrescribed3()));
                }
                if (!StringUtils.isBlank(arvs.getDispensed3())) {
                    arv.setQuantityDispensed3(Double.valueOf(arvs.getDispensed3()));
                }
                if (!StringUtils.isBlank(arvs.getRegimen4())) {
                    Regimen regimen4 = this.regimenRepository.getOne(Long.valueOf(arvs.getRegimen4()));
                    arv.setRegimen4(regimen4);
                    arv.setDuration4(arvs.getDuration4());
                }
                if (!StringUtils.isBlank(arvs.getPrescribed4()) || !StringUtils.isBlank(arvs.getPrescribed4())) {
                    arv.setQuantityPrescribed4(Double.valueOf(arvs.getPrescribed4()));
                }
                if (!StringUtils.isBlank(arvs.getDispensed4()) || !StringUtils.isBlank(arvs.getDispensed4())) {
                    arv.setQuantityPrescribed4(Double.valueOf(arvs.getDispensed4()));
                }
                if (arvs.getAdverseIssue() != null) {
                    arv.setAdverseIssue(Boolean.TRUE);
                } else {
                    arv.setAdverseIssue(Boolean.FALSE);
                }
                arv.setAdverseReport(arvs.getAdverseReport());
                arv.setDateNextClinic(LocalDate.parse(arvs.getDateNextClinic(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                arv.setViralLoadDueDate(arvs.getViralLoadDeuDate());
                if (arvs.getMissedRefill() != null) {
                    arv.setMissedRefill(Boolean.TRUE);
                } else {
                    arv.setMissedRefill(Boolean.FALSE);
                }
                if (!StringUtils.isBlank(arvs.getMissedRefill())) {
                    arv.setMissedRefills(1);
                } else {
                    arv.setMissedRefills(0);
                }
                this.arvRepository.save(arv);
            } else {
                ARV arv = new ARV();
                arv.setUuid(arvs.getUuid());
                arv.setPatient(patient);
                arv.setFacility(facility);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                arv.setDateVisit(LocalDate.parse(arvs.getDateVisit(), formatter));
                arv.setDateNextRefill(LocalDate.parse(arvs.getDateNextRefill(), formatter));
                arv.setBodyWeight(arvs.getBodyWeight());
                arv.setHeight(arvs.getHeight());
                arv.setBp(arvs.getBp());
                arv.setBmi(arvs.getBmi());
                arv.setBmiCategory(arvs.getBmiCategory());
                if (arvs.getId() != null) {
                    arv.setItp(Boolean.TRUE);
                } else {
                    arv.setItp(Boolean.FALSE);
                }
                if (arvs.getHaveYouBeenCoughing() != null) {
                    arv.setCoughing(Boolean.TRUE);
                } else {
                    arv.setCoughing(Boolean.FALSE);
                }
                if (arvs.getDoYouHaveFever() != null) {
                    arv.setFever(Boolean.TRUE);
                } else {
                    arv.setFever(Boolean.FALSE);
                }
                if (arvs.getAreYouLosingWeight() != null) {
                    arv.setWeightLoss(Boolean.TRUE);
                } else {
                    arv.setWeightLoss(Boolean.FALSE);
                }
                if (arvs.getAreYouHavingSweet() != null) {
                    arv.setSweating(Boolean.TRUE);
                } else {
                    arv.setSweating(Boolean.FALSE);
                }
                if (arvs.getDoYouHaveSwellingNeck() != null) {
                    arv.setSwellingNeck(Boolean.TRUE);
                } else {
                    arv.setSwellingNeck(Boolean.FALSE);
                }
                if (arvs.getTbReferred() != null) {
                    arv.setTbReferred(Boolean.TRUE);
                } else {
                    arv.setTbReferred(Boolean.FALSE);
                }
                if (arvs.getEligibleIpt() != null) {
                    arv.setIptEligible(Boolean.TRUE);
                } else {
                    arv.setIptEligible(Boolean.FALSE);
                }
                Regimen regimen1 = this.regimenRepository.getOne(arvs.getRegimen1());
                arv.setRegimen1(regimen1);
                arv.setDuration1(arvs.getDuration1());
                if (!StringUtils.isBlank(arvs.getDispensed1())) {
                    arv.setQuantityDispensed1(Double.valueOf(arvs.getDispensed1()));
                }
                arv.setQuantityPrescribed1(Double.valueOf(arvs.getPrescribed1()));
                Regimen regimen2 = this.regimenRepository.getOne(arvs.getRegimen2());
                arv.setRegimen2(regimen2);
                arv.setDuration2(arvs.getDuration2());
                if (!StringUtils.isBlank(arvs.getPrescribed2()) || !StringUtils.isBlank(arvs.getPrescribed2())) {
                    arv.setQuantityPrescribed2(Double.valueOf(arvs.getPrescribed2()));
                }
                if (!StringUtils.isBlank(arvs.getDispensed2())) {
                    arv.setQuantityDispensed2(Double.valueOf(arvs.getDispensed2()));
                }
                Regimen regimen3 = this.regimenRepository.getOne(arvs.getRegimen3());
                arv.setRegimen3(regimen3);
                arv.setDuration3(arvs.getDuration3());
                if (!StringUtils.isBlank(arvs.getPrescribed3()) || !StringUtils.isBlank(arvs.getPrescribed3())) {
                    arv.setQuantityPrescribed3(Double.valueOf(arvs.getPrescribed3()));
                }
                if (!StringUtils.isBlank(arvs.getDispensed3())) {
                    arv.setQuantityDispensed3(Double.valueOf(arvs.getDispensed3()));
                }
                if (!StringUtils.isBlank(arvs.getRegimen4())) {
                    Regimen regimen4 = this.regimenRepository.getOne(Long.valueOf(arvs.getRegimen4()));
                    arv.setRegimen4(regimen4);
                    arv.setDuration4(arvs.getDuration4());
                }
                if (!StringUtils.isBlank(arvs.getPrescribed4()) || !StringUtils.isBlank(arvs.getPrescribed4())) {
                    arv.setQuantityPrescribed4(Double.valueOf(arvs.getPrescribed4()));
                }
                if (!StringUtils.isBlank(arvs.getDispensed4()) || !StringUtils.isBlank(arvs.getDispensed4())) {
                    arv.setQuantityPrescribed4(Double.valueOf(arvs.getDispensed4()));
                }
                if (arvs.getAdverseIssue() != null) {
                    arv.setAdverseIssue(Boolean.TRUE);
                } else {
                    arv.setAdverseIssue(Boolean.FALSE);
                }
                arv.setAdverseReport(arvs.getAdverseReport());
                arv.setDateNextClinic(LocalDate.parse(arvs.getDateNextClinic(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                arv.setViralLoadDueDate(arvs.getViralLoadDeuDate());
                if (arvs.getMissedRefill() != null) {
                    arv.setMissedRefill(Boolean.TRUE);
                } else {
                    arv.setMissedRefill(Boolean.FALSE);
                }
                if (!StringUtils.isBlank(arvs.getMissedRefill())) {
                    arv.setMissedRefills(1);
                } else {
                    arv.setMissedRefills(0);
                }
                this.arvRepository.save(arv);
            }
        });

        response.setMessage("ARV success");

        return ResponseEntity.ok(response);
    }

    @PostMapping("mobile/save/arv")
    private ResponseEntity<Response> saveARYV(@RequestBody ARVDto arvs) {
        Response response = new Response();
        ARV arv = new ARV();
        Patient patient = this.patientRepository.getOne(arvs.getPatient().getId());
        arv.setPatient(patient);
        Facility facility = this.facilityRepository.getOne(arvs.getFacilityId());
        arv.setFacility(facility);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        arv.setDateVisit(LocalDate.parse(arvs.getDateVisit(), formatter));
        arv.setDateNextRefill(LocalDate.parse(arvs.getDateNextRefill(), formatter));
        arv.setBodyWeight(arvs.getBodyWeight());
        arv.setHeight(arvs.getHeight());
        arv.setBp(arvs.getBp());
        arv.setBmi(arvs.getBmi());
        arv.setBmiCategory(arvs.getBmiCategory());
        arv.setUuid(arvs.getUuid());
        if (arvs.getId() != null) {

            arv.setItp(Boolean.TRUE);
        } else {
            arv.setItp(Boolean.FALSE);
        }
        if (arvs.getHaveYouBeenCoughing() != null) {
            arv.setCoughing(Boolean.TRUE);
        } else {
            arv.setCoughing(Boolean.FALSE);
        }

        if (arvs.getDoYouHaveFever() != null) {
            arv.setFever(Boolean.TRUE);
        } else {
            arv.setFever(Boolean.FALSE);
        }

        if (arvs.getAreYouLosingWeight() != null) {
            arv.setWeightLoss(Boolean.TRUE);
        } else {
            arv.setWeightLoss(Boolean.FALSE);
        }

        if (arvs.getAreYouHavingSweet() != null) {

            arv.setSweating(Boolean.TRUE);
        } else {
            arv.setSweating(Boolean.FALSE);
        }
        if (arvs.getDoYouHaveSwellingNeck() != null) {
            arv.setSwellingNeck(Boolean.TRUE);
        } else {
            arv.setSwellingNeck(Boolean.FALSE);
        }
        if (arvs.getTbReferred() != null) {
            arv.setTbReferred(Boolean.TRUE);
        } else {
            arv.setTbReferred(Boolean.FALSE);
        }
        if (arvs.getEligibleIpt() != null) {
            arv.setIptEligible(Boolean.TRUE);
        } else {
            arv.setIptEligible(Boolean.FALSE);
        }
        Regimen regimen1 = this.regimenRepository.getOne(arvs.getRegimen1());
        arv.setRegimen1(regimen1);
        arv.setDuration1(arvs.getDuration1());
        if (!StringUtils.isBlank(arvs.getDispensed1())) {
            arv.setQuantityDispensed1(Double.valueOf(arvs.getDispensed1()));
        }

        arv.setQuantityPrescribed1(Double.valueOf(arvs.getPrescribed1()));
        Regimen regimen2 = this.regimenRepository.getOne(arvs.getRegimen2());
        arv.setRegimen2(regimen2);
        arv.setDuration2(arvs.getDuration2());
        if (!StringUtils.isBlank(arvs.getPrescribed2()) || !StringUtils.isBlank(arvs.getPrescribed2())) {
            arv.setQuantityPrescribed2(Double.valueOf(arvs.getPrescribed2()));
        }
        if (!StringUtils.isBlank(arvs.getDispensed2())) {
            arv.setQuantityDispensed2(Double.valueOf(arvs.getDispensed2()));
        }
        Regimen regimen3 = this.regimenRepository.getOne(arvs.getRegimen3());
        arv.setRegimen3(regimen3);
        arv.setDuration3(arvs.getDuration3());
        if (!StringUtils.isBlank(arvs.getPrescribed3()) || !StringUtils.isBlank(arvs.getPrescribed3())) {
            arv.setQuantityPrescribed3(Double.valueOf(arvs.getPrescribed3()));
        }
        if (!StringUtils.isBlank(arvs.getDispensed3())) {
            arv.setQuantityDispensed3(Double.valueOf(arvs.getDispensed3()));
        }
        if (!StringUtils.isBlank(arvs.getRegimen4())) {
            Regimen regimen4 = this.regimenRepository.getOne(Long.valueOf(arvs.getRegimen4()));
            arv.setRegimen4(regimen4);
            arv.setDuration4(arvs.getDuration4());
        }

        /* 481 */
        if (!StringUtils.isBlank(arvs.getPrescribed4()) || !StringUtils.isBlank(arvs.getPrescribed4())) {
            /* 482 */
            arv.setQuantityPrescribed4(Double.valueOf(arvs.getPrescribed4()));
        }
        /* 484 */
        if (!StringUtils.isBlank(arvs.getDispensed4()) || !StringUtils.isBlank(arvs.getDispensed4())) {
            /* 485 */
            arv.setQuantityPrescribed4(Double.valueOf(arvs.getDispensed4()));
        }
        /* 487 */
        if (arvs.getAdverseIssue() != null) {
            /* 488 */
            arv.setAdverseIssue(Boolean.TRUE);
        } else {
            /* 490 */
            arv.setAdverseIssue(Boolean.FALSE);
        }
        /* 492 */
        arv.setAdverseReport(arvs.getAdverseReport());
        /* 493 */
        arv.setDateNextClinic(LocalDate.parse(arvs.getDateNextClinic(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        /* 494 */
        arv.setViralLoadDueDate(arvs.getViralLoadDeuDate());
        /* 495 */
        if (arvs.getMissedRefill() != null) {
            /* 496 */
            arv.setMissedRefill(Boolean.TRUE);
        } else {
            /* 498 */
            arv.setMissedRefill(Boolean.FALSE);
        }
        /* 500 */
        if (!StringUtils.isBlank(arvs.getMissedRefill())) {
            /* 501 */
            arv.setMissedRefills(1);
        } else {
            /* 503 */
            arv.setMissedRefills(0);
        }

        /* 506 */
        this.arvRepository.save(arv);
        /* 507 */
        response.setMessage("ARV success");
        /* 508 */
        return ResponseEntity.ok(response);
    }


    @GetMapping("mobile/patient/{deviceId}/{pin}/{accountUserName}/{accountPassword}")
    private ResponseEntity<Map<String, Object>> activateOutLet(@PathVariable("deviceId") String deviceId, @PathVariable("pin") String pin, @PathVariable("accountUserName") String accountUserName, @PathVariable("accountPassword") String accountPassword) {

        CommunityPharmacy communityPharmacy = null;
        try {

            communityPharmacy = this.communityPharmacyRepository.findByPinIgnoreCase(pin).orElseThrow(() -> new ResourceException("Activation pin does not  exist:   " + pin));

        } catch (ResourceException resourceException) {

            resourceException.printStackTrace();
        }
        Facility facility = this.facilityRepository.getOne(Objects.<Long>requireNonNull(Objects.requireNonNull(communityPharmacy).getFacility().getId()));
        DeviceConfig deviceConfig1 = this.deviceConfigRepository.findByDeviceId(deviceId);

        if (deviceConfig1 == null) {
            DeviceConfig deviceconfig = new DeviceConfig();
            deviceconfig.setDeviceId(deviceId);
            deviceconfig.setUsername(accountUserName);
            deviceconfig.setPassword(accountPassword);
            deviceconfig.setFacility(facility);
            this.deviceConfigRepository.save(deviceconfig);
        }
        List<PatientDto> patientList = new ArrayList<>();
        List<Patient> patients = this.patientRepository.findByCommunityPharmacyAndArchived(communityPharmacy, Boolean.FALSE);
        patients.forEach(patient -> {
            PatientDto patient1 = new PatientDto();
            patient1.setId(patient.getId());
            patient1.setHospitalNum(patient.getHospitalNum());
            patient1.setFacility(patient.getFacility());
            patient1.setUniqueId(patient.getUniqueId());
            patient1.setSurname(patient.getSurname());
            patient1.setOtherNames(patient.getOtherNames());
            patient1.setGender(patient.getGender());
            String dob = patient.getDateBirth().format(df);
            patient1.setDateBirth(dob);
            patient1.setAddress(patient.getAddress());
            patient1.setPhone(patient.getPhone());
            patient1.setDateStarted(patient.getDateStarted().format(df));
            patient1.setLastClinicStage(patient.getLastClinicStage());
            patient1.setLastViralLoad(patient.getLastViralLoad());
            patient1.setDateLastViralLoad(patient.getDateLastViralLoad().format(df));
            patient1.setViralLoadDueDate(patient.getViralLoadDueDate().format(df));
            patient1.setViralLoadType(patient.getViralLoadType());
            patient1.setDateLastClinic(patient.getDateLastClinic().format(df));
            patient1.setDateNextClinic(patient.getDateNextClinic().format(df));
            patient1.setDateLastRefill(patient.getDateLastRefill().format(df));
            patient1.setDateNextRefill(patient.getDateNextRefill().format(df));
            patient1.setPharmacyId(patient.getCommunityPharmacy().getId());
            patientList.add(patient1);
        });

        List<RegimenDto> regimenList = new ArrayList<>();
        List<Inventory> regimenList1 = this.inventoryRepository.findByCommunityPharmacy(communityPharmacy);
        regimenList1.forEach(inventory -> {
            RegimenDto regimen = new RegimenDto();
            regimen.setId(inventory.getRegimen().getId());
            regimen.setRegimenTypeId(inventory.getRegimen().getRegimenTypeId());
            regimen.setName(inventory.getRegimen().getName());
            regimen.setBatchNumber(inventory.getBatchNumber());
            regimen.setQuantity(inventory.getQuantity());
            regimen.setExpireDate(inventory.getExpireDate());
            regimenList.add(regimen);
        });
        Map<String, Object> obj = new HashMap<>();
        obj.put("facility", facility);
        obj.put("regimens", regimenList);
        obj.put("patients", patientList);
        return ResponseEntity.ok(obj);
    }

    @GetMapping("mobile/pharmacy/{pin}")
    private ResponseEntity<Map<String, Object>> activatePharmacyAccount(@PathVariable("pin") String pin) {
        CommunityPharmacy communityPharmacy = null;
        try {
            /* 574 */
            communityPharmacy = this.communityPharmacyRepository.findByPinIgnoreCase(pin).orElseThrow(() -> new ResourceException("Activation pin does not  exist:   " + pin));
            /* 575 */
        } catch (ResourceException resourceException) {
            /* 576 */
            resourceException.printStackTrace();
        }

        /* 579 */
        Map<String, Object> obj = new HashMap<>();
        /* 580 */
        obj.put("pharmacy", communityPharmacy);
        /* 581 */
        return ResponseEntity.ok(obj);
    }


    @GetMapping("mobile/facility/{deviceId}/{accountUserName}/{accountPassword}")
    private ResponseEntity<Map<String, Object>> activateFacility(@PathVariable("deviceId") String deviceId, @PathVariable("accountUserName") String accountUserName, @PathVariable("accountPassword") String accountPassword) {

        List<Facility> facility = this.facilityRepository.findAll();
        List<Regimen> regimenList = this.regimenRepository.findByNameOrderByIdDesc("TDF 300mg +3TC 300mg +DTG 50mg");
        List<District> districtList = this.districtRepository.findAll();
        List<DistrictDto> outPutDistrict = new ArrayList<>();

        districtList.forEach(district -> {
            DistrictDto dto = new DistrictDto();
            dto.setName(district.getName());
            dto.setId(district.getId());
            dto.setStateId(district.getState().getId());
            outPutDistrict.add(dto);
        });
        List<FacilityDto> facilityDto = new ArrayList<>();
        for (Facility facility1 : facility) {
            FacilityDto facilityDto1 = new FacilityDto();
            facilityDto1.setId(facility1.getId());
            facilityDto1.setName(facility1.getName());
            facilityDto1.setStateId(facility1.getState().getId());
            facilityDto1.setDistrictId(facility1.getDistrict().getId());
            facilityDto.add(facilityDto1);
            DeviceConfig deviceConfig = this.deviceConfigRepository.findByDeviceId(deviceId);
            if (deviceConfig == null) {
                DeviceConfig deviceconfig = new DeviceConfig();
                deviceconfig.setDeviceId(deviceId);
                deviceconfig.setUsername(accountUserName);
                deviceconfig.setPassword(accountPassword);
                deviceconfig.setFacility(facility1);
                this.deviceConfigRepository.save(deviceconfig);
            }
        }
        List<CommunityPharmacy> communityPharmacies = this.communityPharmacyRepository.findAll();
        List<State> states = this.stateRepository.findAll();
        Map<String, Object> obj = new HashMap<>();
        obj.put("facility", facilityDto);
        obj.put("state", states);
        obj.put("district", outPutDistrict);
        obj.put("regimens", regimenList);
        obj.put("communityPharmacies", communityPharmacies);
        return ResponseEntity.ok(obj);
    }

    @GetMapping("mobile/login/{username}/{password}/{role}")
    private ResponseEntity<Response> login(@PathVariable String username, @PathVariable String password, @PathVariable String role) {
        Response response = new Response();
        Optional<User> user = this.userRepository.findByUsernameAndPasswordIgnoreCase(username, password);
        if (user.isPresent()) {
            User user2 = user.get();
            UserDto userDto = new UserDto();
            userDto.setUsername(user2.getUsername());
            userDto.setPassword(user2.getPassword());
            userDto.setRole(user2.getRole());
            Facility facility1 = this.facilityRepository.getOne(Objects.<Long>requireNonNull(user2.getFacility().getId()));
            userDto.setFacilityName(facility1.getName());
            userDto.setFacilityId(facility1.getId());
            response.setUser(userDto);
            return ResponseEntity.ok(response);
        }
        User user1 = new User();
        user1.setRole(role);
        user1.setUsername(username);
        user1.setPassword(password);
        Facility facility = this.facilityRepository.getOne(1523L);
        user1.setFacility(facility);
        User result1 = this.userRepository.save(user1);
        UserDto result = new UserDto();
        result.setUsername(result1.getUsername());
        result.setPassword(result1.getPassword());
        result.setRole(result1.getRole());
        result.setFacilityName(facility.getName());
        result.setFacilityId(facility.getId());
        response.setUser(result);
        return ResponseEntity.ok(response);
    }


    public int getAge(String dateBirth) {
        LocalDate currentDate = LocalDate.parse(dateBirth);
        LocalDate pdate = LocalDate.of(currentDate.getYear(), currentDate.getMonthValue(), currentDate.getDayOfMonth());
        LocalDate now = LocalDate.now();
        Period diff = Period.between(pdate, now);
        return diff.getYears();
    }

    @PostMapping("mobile/discontinue/{dateDiscontinue}/{reasonDiscontinued}/{id}")
    public ResponseEntity<Response> discontinued(@PathVariable("dateDiscontinue") String dateDiscontinue, @PathVariable("reasonDiscontinued") String reasonDiscontinued, @PathVariable("id") Long id) {
        Response response = new Response();
        updatePatient(LocalDate.parse(dateDiscontinue, DateTimeFormatter.ofPattern("yyyy-MM-dd")), reasonDiscontinued, id);
        response.setMessage("Success");
        return ResponseEntity.ok(response);
    }


    @GetMapping("get-pharmacy/{facilityId}")
    public ResponseEntity<List<CommunityPharmacy>> getPharmacy(@PathVariable("facilityId") Long facilityId) {
        Facility facility = this.facilityRepository.findById(facilityId).get();
        List<CommunityPharmacy> communityPharmacies = this.communityPharmacyRepository.findByFacility(facility);
        return ResponseEntity.ok(communityPharmacies);
    }

    @GetMapping("get-facility-all")
    public ResponseEntity<List<FacilityDto>> getFacility() {
        List<Facility> facilities = this.facilityRepository.findAll();
        List<FacilityDto> facilityDto1 = new ArrayList<>();
        facilities.forEach(facility -> {
            FacilityDto facilityDto = new FacilityDto();
            facilityDto.setId(facility.getId());
            facilityDto.setName(facility.getName());
            facilityDto.setStateId(facility.getState().getId());
            facilityDto.setDistrictId(facility.getDistrict().getId());
            facilityDto1.add(facilityDto);
        });
        return ResponseEntity.ok(facilityDto1);
    }


    public void updatePatient(LocalDate dateDiscontinue, String reasonDiscontinued, Long id) {
        jdbcTemplate.update("update patient set date_discontinued = ?, reason_discontinued = ? , discontinued = ? where id = ?", dateDiscontinue, reasonDiscontinued, 1, id);
    }

    @DeleteMapping("mobile-patient/delete/{hospitalNum}/{facilityId}")
    public void deletePatient(@PathVariable String hospitalNum, @PathVariable Long facilityId) {
        Facility facility = this.facilityRepository.findById(facilityId).get();
        Optional<Patient> patient1 = this.patientRepository.findByUniqueIdAndFacility(hospitalNum, facility);
        if (patient1.isPresent()) {
            Patient patient = patient1.get();
            patient.setArchived(Boolean.TRUE);
            this.patientRepository.save(patient);
        }
    }

    ///alter table patient
    //  alter date_next_refill type date using(date_next_refill::date)
    @GetMapping("mobile-patient/all")
    public ResponseEntity<Map<String, Object>> getPatient() {
        List<PatientDto> patientList = new ArrayList<>();
        List<Patient> patients = this.patientRepository.findByArchived(Boolean.FALSE);
        patients.forEach(patient -> {
            PatientDto patient1 = new PatientDto();
            patient1.setId(patient.getId());
            patient1.setHospitalNum(patient.getHospitalNum());
            patient1.setFacility(patient.getFacility());
            patient1.setUniqueId(patient.getUniqueId());
            patient1.setSurname(patient.getSurname());
            patient1.setOtherNames(patient.getOtherNames());
            patient1.setGender(patient.getGender());
            String dob = patient.getDateBirth().format(df);
            patient1.setDateBirth(dob);
            patient1.setAddress(patient.getAddress());
            patient1.setPhone(patient.getPhone());
            patient1.setDateStarted(patient.getDateStarted().format(df));
            patient1.setLastClinicStage(patient.getLastClinicStage());
            patient1.setLastViralLoad(patient.getLastViralLoad());
            patient1.setDateLastViralLoad(patient.getDateLastViralLoad().format(df));
            patient1.setViralLoadDueDate(patient.getViralLoadDueDate().format(df));
            patient1.setViralLoadType(patient.getViralLoadType());
            patient1.setDateLastClinic(patient.getDateLastClinic().format(df));
            patient1.setDateNextClinic(patient.getDateNextClinic().format(df));
            patient1.setDateLastRefill(patient.getDateLastRefill().format(df));
            patient1.setDateNextRefill(patient.getDateNextRefill().format(df));
            String expectedDate = patient.getDateNextRefill().format(df);//"06/12/2022";
            System.out.println("ExpectedDate" + expectedDate);
            String actualDate = LocalDate.now().format(df);
            System.out.println("actualDate " + actualDate);
            if (expectedDate.compareToIgnoreCase(actualDate) == 0) {
                System.out.println("Done on the right Date");
                patient1.setBlue("blue");
            }
//
            if (expectedDate.compareToIgnoreCase(actualDate) < 0) {
                System.out.println("Done after the right Date");
                System.out.println("Red");
                patient1.setRed("red");

            }

            if (expectedDate.compareToIgnoreCase(actualDate) > 0) {
                System.out.println("Done before the right Date");
                System.out.println("green");
                patient1.setGreen("green");
            }


            patient1.setPharmacyId(patient.getCommunityPharmacy().getId());
            patient1.setUuid(patient.getUuid());
            patientList.add(patient1);
        });
        Map<String, Object> obj = new HashMap<>();
        obj.put("patients", patientList);
        return ResponseEntity.ok(obj);
    }

    @DeleteMapping("mobile-patient-id/delete/{id}")
    public void deletePatient(@PathVariable Long id) {
        Optional<Patient> patient1 = this.patientRepository.findById(id);
        if (patient1.isPresent()) {
            Patient patient = patient1.get();
            this.patientRepository.delete(patient);
        }
    }

    public Date parseDate(String strDate) throws Exception {
        if (strDate != null && !strDate.isEmpty()) {
            SimpleDateFormat[] formats =
                new SimpleDateFormat[]{new SimpleDateFormat("yyyy-MM-dd"), new SimpleDateFormat("dd-MM-yyyy"), new SimpleDateFormat("MM-dd-yyyy"), new SimpleDateFormat("yyyyMMdd"),
                    new SimpleDateFormat("MM/dd/yyyy")};

            Date parsedDate = null;

            for (int i = 0; i < formats.length; i++) {
                try {
                    parsedDate = formats[i].parse(strDate);
                    return parsedDate;
                } catch (ParseException e) {
                    continue;
                }
            }
        }
        throw new Exception("Unknown date format: '" + strDate + "'");
    }

    @GetMapping("mobile-arv/all")
    public ResponseEntity<Map<String, Object>> getARV() {
        List<ARVDto> arvDtoList = new ArrayList<>();
        List<ARV> arvList = this.arvRepository.findByArchived(Boolean.FALSE);
        arvList.forEach(arvs -> {
            ARVDto arv = new ARVDto();
            arv.setId(arvs.getId());
            arv.setPatientId(arvs.getPatient().getId());
            arv.setFacilityId(arvs.getFacility().getId());
            arv.setDateVisit(String.valueOf(arvs.getDateVisit()));
            arv.setDateNextRefill(String.valueOf(arvs.getDateNextRefill()));
            arvDtoList.add(arv);
        });
        Map<String, Object> obj = new HashMap<>();
        obj.put("arvList", arvDtoList);
        return ResponseEntity.ok(obj);
    }

    @GetMapping("mobile-patient/update-date")
    private void removePatientByUniqueIdAndFacility(@RequestParam(value = "dateNextRefill") String dateNextRefill, @RequestParam(value = "id") Long id) throws ResourceException {
        Optional<Patient> patient = this.patientRepository.findById(id);
        patient.map(patient1 -> {
            patient1.setDateNextRefill(LocalDate.parse(dateNextRefill, df));
            this.patientRepository.save(patient1);
            return ResponseEntity.ok();
        }).orElseThrow(() -> new ResourceException("ID   does not  exist:   " + id + "   "));
    }

    @PostMapping("mobile/save/inventory")
    private ResponseEntity<Response> saveInventory(@RequestBody InventoryDto inventory) {
        Response response = new Response();
        Regimen regimen2 = this.regimenRepository.getOne(inventory.getRegimenId());
        Inventory issuedDrug = this.inventoryRepository.findByRegimen(regimen2);
        if (issuedDrug != null) {
            issuedDrug.setId(issuedDrug.getId());
            issuedDrug.setBatchNumber(inventory.getBatchNumber());
            Regimen regimen = this.regimenRepository.getOne(inventory.getRegimenId());
            issuedDrug.setRegimen(regimen);
            Optional<CommunityPharmacy> outlet = this.communityPharmacyRepository.findByPinIgnoreCase(inventory.getPinCode());
            if (outlet.isPresent()) {
                CommunityPharmacy outlet1 = outlet.get();
                issuedDrug.setCommunityPharmacy(outlet1);
            }
            issuedDrug.setExpireDate(inventory.getExpireDate());
            issuedDrug.setQuantity(inventory.getQuantity());
            this.inventoryRepository.save(issuedDrug);
            response.setMessage("inventory updated");
        } else {
            Inventory issuedDrug1 = new Inventory();
            issuedDrug1.setBatchNumber(inventory.getBatchNumber());
            Regimen regimen = this.regimenRepository.getOne(inventory.getRegimenId());
            issuedDrug1.setRegimen(regimen);
            Optional<CommunityPharmacy> outlet = this.communityPharmacyRepository.findByPinIgnoreCase(inventory.getPinCode());
            if (outlet.isPresent()) {
                CommunityPharmacy outlet1 = outlet.get();
                issuedDrug1.setCommunityPharmacy(outlet1);
            }
            issuedDrug1.setExpireDate(inventory.getExpireDate());
            issuedDrug1.setQuantity(inventory.getQuantity());
            this.inventoryRepository.save(issuedDrug1);
            response.setMessage("inventory success");
        }

        return ResponseEntity.ok(response);
    }


    @PostMapping("mobile/save/drug")
    private ResponseEntity<Response> saveDrug(@RequestBody DrugDto drug) {
        Response response = new Response();
        Drug drug2 = this.drugRepository.findByName(drug.getDrugName());
        if (drug2 != null) {
            Drug drug1 = new Drug();
            Regimen regimen = this.regimenRepository.getOne(drug.getRegimeId());
            drug1.setRegimen(regimen);
            drug1.setName(drug.getDrugName());
            drug1.setBasicUnit(drug.getBasicUnit());
            drug1.setId(drug2.getId());
            this.drugRepository.save(drug1);
        } else {
            Drug drug1 = new Drug();
            Regimen regimen = this.regimenRepository.getOne(drug.getRegimeId());
            drug1.setRegimen(regimen);
            drug1.setName(drug.getDrugName());
            drug1.setBasicUnit(drug.getBasicUnit());
            this.drugRepository.save(drug1);
        }
        response.setMessage("Drug success");
        return ResponseEntity.ok(response);
    }

    @GetMapping("mobile/regimen-name")
    public List<Regimen> getRegimenByName(@RequestParam String name) {
        return this.regimenRepository.findByNameOrderByIdDesc(name);
    }

    @GetMapping("mobile/regimen-id/{id}")
    public Regimen getRegimen(@PathVariable Long id) {
        return this.regimenRepository.getOne(id);
    }

    @GetMapping("mobile/regimen-all")
    public List<Regimen> getRegimenAll() {
        return this.regimenRepository.findAll();
    }
}

