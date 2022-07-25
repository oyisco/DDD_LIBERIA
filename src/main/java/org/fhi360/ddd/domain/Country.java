package org.fhi360.ddd.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.domain.Persistable;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@EqualsAndHashCode(of = "id")
public class Country implements Serializable, Persistable<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(unique = true, name = "name")
    private String name;
    @Column(unique = true, name = "code")
    private String code;

    @Override
    public boolean isNew() {
        return id == null;
    }
}
