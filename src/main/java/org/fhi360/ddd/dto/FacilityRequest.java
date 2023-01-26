package org.fhi360.ddd.dto;

import lombok.Data;

@Data
public class FacilityRequest {
    String name;
    Long stateId;
    Long lgaId;
}
