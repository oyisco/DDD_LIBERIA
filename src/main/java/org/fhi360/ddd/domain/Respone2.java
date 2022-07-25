package org.fhi360.ddd.domain;

import lombok.Data;
import org.fhi360.ddd.dto.PatientDto;
import org.fhi360.ddd.dto.UserDto;
@Data
public class Respone2 {
    private String message;
    private CommunityPharmacy pharmacy;
    private UserDto user;
    private Facility facility;
    private PatientDto patient;
}
