package org.fhi360.ddd.domain;

import javax.persistence.Column;

import lombok.Data;
import org.springframework.data.domain.Persistable;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@Entity
public class DeviceConfig implements Serializable, Persistable<Long> {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @JoinColumn(name = "facility_id")
    @ManyToOne
    private Facility facility;
    @NotNull
    @Column(name = "device_id")
    private String deviceId;
    @Column(name = "username")
    private String username;
    @Column(name = "password")
    private String password;
    public boolean isNew() {
        return (this.id == null);
    }
}
