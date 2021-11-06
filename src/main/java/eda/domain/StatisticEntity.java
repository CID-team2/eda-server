package eda.domain;

import com.vladmihalcea.hibernate.type.json.JsonType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
@Entity
public class StatisticEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private DatasetColumn column;

    @Enumerated(EnumType.STRING)
    private FeatureType featureType;

    @Enumerated(EnumType.STRING)
    private Statistic.Kind kind;

    @Type(type = "json")
    @Column(columnDefinition = "json")
    private Map<String, Object> value;
}
