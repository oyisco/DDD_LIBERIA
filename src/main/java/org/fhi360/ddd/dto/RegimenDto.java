package org.fhi360.ddd.dto;

import lombok.Data;

import javax.persistence.Column;
@Data
public class RegimenDto {
    private Long id;
    private String name;
    private Long regimenTypeId;
    private String batchNumber;
    private String expireDate;
    private Long quantity;
}
