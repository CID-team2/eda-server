package eda.domain;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Entity
public class FeatureView extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "feature_view_id")
    private List<Feature> features;

    public void update(FeatureView featureView) {
        name = featureView.name;
        features.clear();
        features.addAll(featureView.getFeatures());
    }
}
