package org.fhi360.ddd.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Data
@Table(name = "users") public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
   private Long id;
    @Column(name = "username")
   private String username;
    @Column(name = "password")
    private String password;
    @Column(name = "role")
    private String role;
    @JoinColumn(name = "facility_id")
    @ManyToOne
    @JsonIgnore
    private Facility facility;
}
