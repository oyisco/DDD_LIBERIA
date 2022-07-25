package org.fhi360.ddd.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import javax.persistence.*;

import com.sun.istack.NotNull;
import lombok.Data;
import org.springframework.data.domain.Persistable;

@Data
@Entity
public final class State implements Serializable, Persistable<Long> {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "name", nullable = false)
    @NotNull
    private String name;
    @JoinColumn(name = "country_id")
    @ManyToOne
    @JsonIgnore
    private Country country;

    @JsonIgnore
    public boolean isNew() {
        return (this.id == null);
    }
}
