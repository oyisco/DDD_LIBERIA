package org.fhi360.ddd.domain;

import org.fhi360.ddd.dto.UserDto;

@lombok.Data
public class Response {
  private String message;
  private CommunityPharmacy pharmacy;
  private Patient patient;
  private UserDto user;
  private Facility facility;
}
