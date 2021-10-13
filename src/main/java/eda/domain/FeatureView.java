package eda.domain;

import lombok.Getter;

import javax.persistence.*;
import java.util.Set;

@Getter
@Entity
public class FeatureView {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "featureView")
    private Set<Feature> features;
}
