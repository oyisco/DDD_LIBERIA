package org.fhi360.ddd.web;

import liquibase.pro.packaged.D;
import liquibase.pro.packaged.U;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.fhi360.ddd.domain.*;
import org.fhi360.ddd.dto.*;
import org.fhi360.ddd.repositories.*;
import org.fhi360.ddd.utils.EmailSender;
import org.fhi360.ddd.utils.ResourceException;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import javax.mail.MessagingException;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/ddd/")
@RequiredArgsConstructor
@Slf4j
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
            String message = emailSender.activation(communityPharmacy.getName(), communityPharmacy.getUsername(), activationCode);
            emailSender.sendMail(communityPharmacy.getEmail(), "DDD Activation", message);
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
            Facility facility = this.facilityRepository.getOne(Objects.requireNonNull(patient.getFacility().getId()));
            Optional<Patient> patient1 = this.patientRepository.findByHospitalNumAndFacility(patient.getHospitalNum(), facility);
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
    private ResponseEntity<Response> savePatient(@RequestBody Patient patient) {
        Response response = new Response();
        Facility facility = this.facilityRepository.getOne(Objects.requireNonNull(patient.getFacility().getId()));
        Optional<Patient> patient1 = this.patientRepository.findByHospitalNumAndFacility(patient.getHospitalNum(), facility);
        if (patient1.isPresent()) {
            response.setMessage("Patient already exist");
            return ResponseEntity.ok(response);
        }
        //System.out.println("COMMUNITYPHARMACY" + patient.getPharmacyId());
        CommunityPharmacy communityPharmacy = this.communityPharmacyRepository.getOne(patient.getPharmacyId());
        patient.setCommunityPharmacy(communityPharmacy);
        this.patientRepository.save(patient);
        patient.setPharmacyId(patient.getPharmacyId());
        response.setPatient(patient);
        return ResponseEntity.ok(response);
    }

    //api/ddd/
    @PostMapping("mobile/sync/arv")
    private ResponseEntity<Response> syncARV(@RequestBody List<ARVDto> arvDtos) {
        Response response = new Response();
        arvDtos.forEach(arvs -> {
            Facility facility = this.facilityRepository.getOne(arvs.getFacilityId());
            Patient patient = this.patientRepository.getOne(arvs.getPatient().getId());
            Optional<ARV> checkIfExist = this.arvRepository.findByPatientAndFacility(patient, facility);
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
                    Regimen regimen4 = regimenRepository.getOne(Long.valueOf(arvs.getRegimen4()));
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
                    Regimen regimen4 = regimenRepository.getOne(Long.valueOf(arvs.getRegimen4()));
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
            Regimen regimen4 = regimenRepository.getOne(Long.valueOf(arvs.getRegimen4()));
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
        response.setMessage("ARV success");
        return ResponseEntity.ok(response);
    }


    //mobile/patient/{deviceId}/{pin}/{accountUserName}/{accountPassword}
    @GetMapping("mobile/patient/{deviceId}/{pin}/{accountUserName}/{accountPassword}")

    private ResponseEntity<Map<String, Object>> activateOutLet(@PathVariable("deviceId") String deviceId,
                                                               @PathVariable("pin") String pin,
                                                               @PathVariable("accountUserName") String accountUserName,
                                                               @PathVariable("accountPassword") String accountPassword) {
        CommunityPharmacy communityPharmacy = null;
        try {
            communityPharmacy = this.communityPharmacyRepository.findByPinIgnoreCase(pin).orElseThrow(() -> new ResourceException("Activation pin does not  exist:   " + pin));
        } catch (ResourceException resourceException) {
            resourceException.printStackTrace();
        }
        Facility facility = this.facilityRepository.getOne(Objects.requireNonNull(Objects.requireNonNull(communityPharmacy).getFacility().getId()));
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
        List<Patient> patients = this.patientRepository.findByFacilityAndCommunityPharmacy(facility, communityPharmacy);
        patients.forEach(patient -> {
            PatientDto patient1 = new PatientDto();
            patient1.setId(patient.getId());
            patient1.setHospitalNum(patient.getHospitalNum());
            patient1.setFacility(patient.getFacility());
            patient1.setUniqueId(patient.getUniqueId());
            patient1.setSurname(patient.getSurname());
            patient1.setOtherNames(patient.getOtherNames());
            patient1.setGender(patient.getGender());
            patient1.setDateBirth(String.valueOf(patient.getDateBirth()));
            patient1.setAddress(patient.getAddress());
            patient1.setPhone(patient.getPhone());
            patient1.setDateStarted(patient.getDateStarted());
            patient1.setLastClinicStage(patient.getLastClinicStage());
            patient1.setLastViralLoad(patient.getLastViralLoad());
            patient1.setDateLastViralLoad(patient.getDateLastViralLoad());
            patient1.setViralLoadDueDate(patient.getViralLoadDueDate());
            patient1.setViralLoadType(patient.getViralLoadType());
            patient1.setDateLastClinic(patient.getDateLastClinic());
            patient1.setDateNextClinic(patient.getDateNextClinic());
            patient1.setDateLastRefill(patient.getDateLastRefill());
            patient1.setDateNextRefill(patient.getDateNextRefill());
            patient1.setPharmacyId(patient.getCommunityPharmacy().getId());
            patientList.add(patient1);

        });
        List<Regimen> regimenList = this.regimenRepository.findAll();
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
            communityPharmacy = this.communityPharmacyRepository.findByPinIgnoreCase(pin).orElseThrow(() -> new ResourceException("Activation pin does not  exist:   " + pin));
        } catch (ResourceException resourceException) {
            resourceException.printStackTrace();
        }
        // Facility facility = this.facilityRepository.getOne(Objects.requireNonNull(Objects.requireNonNull(communityPharmacy).getFacility().getId()));
        Map<String, Object> obj = new HashMap<>();
        obj.put("pharmacy", communityPharmacy);
        return ResponseEntity.ok(obj);
    }

    //api/ddd/mobile/patient/{deviceId}/{pin}/{accountUserName}/{accountPassword}
    @GetMapping("mobile/facility/{deviceId}/{facilityId}/{accountUserName}/{accountPassword}")
    private ResponseEntity<Map<String, Object>> activateFacility(@PathVariable("deviceId") String deviceId,
                                                                 @PathVariable("facilityId") Long facilityId,
                                                                 @PathVariable("accountUserName") String accountUserName,
                                                                 @PathVariable("accountPassword") String accountPassword) {

        Facility facility = null;
        try {
            facility = this.facilityRepository.findById(facilityId).orElseThrow(() -> new ResourceException("Invalid facility code " + facilityId));
        } catch (ResourceException e) {
            e.printStackTrace();
        }
        DeviceConfig deviceConfig = this.deviceConfigRepository.findByDeviceId(deviceId);
        if (deviceConfig == null) {
            DeviceConfig deviceconfig = new DeviceConfig();
            deviceconfig.setDeviceId(deviceId);
            deviceconfig.setUsername(accountUserName);
            deviceconfig.setPassword(accountPassword);
            deviceconfig.setFacility(facility);
            this.deviceConfigRepository.save(deviceconfig);
        }

        List<Regimen> regimenList = this.regimenRepository.findAll();
        List<District> districtList = this.districtRepository.findByState(Objects.requireNonNull(facility).getState());
        List<DistrictDto> outPutDistrict = new ArrayList<>();
        districtList.forEach(district -> {
            DistrictDto dto = new DistrictDto();
            dto.setName(district.getName());
            dto.setId(district.getId());
            dto.setStateId(district.getState().getId());
            outPutDistrict.add(dto);
        });

        FacilityDto facilityDto = new FacilityDto();
        facilityDto.setId(facility.getId());
        facilityDto.setName(facility.getName());
        facilityDto.setStateId(facility.getState().getId());
        facilityDto.setDistrictId(facility.getDistrict().getId());
        Map<String, Object> obj = new HashMap<>();
        obj.put("facility", facilityDto);
        obj.put("state", Objects.requireNonNull(facility).getState());
        obj.put("district", outPutDistrict);
        obj.put("regimens", regimenList);
        return ResponseEntity.ok(obj);
    }

    @GetMapping("mobile/login/{username}/{password}")
    private ResponseEntity<Response> login(@PathVariable String username, @PathVariable String password) {
        Response response = new Response();
        Optional<User> user = this.userRepository.findByUsernameAndPasswordIgnoreCase(username, password);
        if (user.isPresent()) {
            User user1 = user.get();
            UserDto result = new UserDto();
            result.setUsername(user1.getUsername());
            result.setPassword(user1.getPassword());
            result.setRole(user1.getRole());
            Facility facility = this.facilityRepository.getOne(Objects.requireNonNull(user1.getFacility().getId()));
            result.setFacilityName(facility.getName());
            result.setFacilityId(facility.getId());
            response.setUser(result);
            return ResponseEntity.ok(response);
        } else {

            response.setMessage("User not found");
        }
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
        this.updatePatient(LocalDate.parse(dateDiscontinue, DateTimeFormatter.ofPattern("yyyy-MM-dd")), reasonDiscontinued, id);
        response.setMessage("Success");
        return ResponseEntity.ok(response);
    }


    public void updatePatient(LocalDate dateDiscontinue, String reasonDiscontinued, Long id) {
        jdbcTemplate.update("update patient set date_discontinued = ?, reason_discontinued = ? , discontinued = ? where id = ?", dateDiscontinue, reasonDiscontinued, 1, id);
    }
}
