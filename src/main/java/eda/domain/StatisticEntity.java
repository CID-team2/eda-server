package eda.domain;

import com.vladmihalcea.hibernate.type.json.JsonType;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.util.Map;

@TypeDef(name = "json", typeClass = JsonType.class)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "key",
                        columnNames = {"dataset_column_id", "feature_type", "kind"}
                )
        }
)
@Entity
public class StatisticEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "dataset_column_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private DatasetColumn column;

    @Enumerated(EnumType.STRING)
    @Column(name = "feature_type")
    private FeatureType featureType;

    @Enumerated(EnumType.STRING)
    @Column(name = "kind")
    private Statistic.Kind kind;

    @Setter
    @Type(type = "json")
    @Column(columnDefinition = "json")
    private Map<String, Object> value;
}
