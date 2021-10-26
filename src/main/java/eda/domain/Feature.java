package eda.domain;

import eda.data.DataReader;
import eda.data.StatisticsCalculator;
import eda.dto.StatisticRequestDto;
import lombok.*;
import org.apache.commons.lang.NotImplementedException;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.beans.factory.annotation.Autowired;

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

    private String columnName;

    private DataType dataType;

    private FeatureType featureType;

    @ElementCollection
    @CollectionTable(name = "feature_tag", joinColumns = @JoinColumn(name = "feature_id"))
    private Set<String> tags;




}
