package eda.domain;

import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.util.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Entity
public class Feature {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "parent_id")
    private Set<Feature> children;

    private String name;

    @ManyToOne
    @JoinColumn(name = "dataset_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Dataset dataset;

    @ManyToOne
    @JoinColumn(name = "dataset_column_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private DatasetColumn column;

    @Enumerated(EnumType.STRING)
    private FeatureType featureType;

    @ElementCollection
    @CollectionTable(name = "feature_tag", joinColumns = @JoinColumn(name = "feature_id"))
    private Set<String> tags;

    public DataType getDataType() {
        return column.getDataType();
    }

    public String getColumnName() {
        return column.getName();
    }
}
