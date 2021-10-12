package eda.domain;

import eda.data.ORCReader;
import lombok.Getter;

import javax.persistence.*;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Getter
@Entity
public class Feature {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "parent_id")
    private Set<Feature> children;

    private String name;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "dataset_id")
    private Dataset dataset;

    private String columnName;

    private DataType dataType;

    private FeatureType featureType;

    @ManyToOne
    @JoinColumn(name = "feature_view_id")
    private FeatureView featureView;

    @ElementCollection
    @CollectionTable(name = "feature_tag", joinColumns = @JoinColumn(name = "feature_id"))
    private Set<String> tags;

    public List<Object> readValues() {
        List<String> valuesString;
        try {
            ORCReader orcReader = new ORCReader(dataset.getPath());
            valuesString = orcReader.readColumn(columnName);
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }

        return dataType.convertStringList(valuesString);
    }
}
