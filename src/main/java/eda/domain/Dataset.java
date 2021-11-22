package eda.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Entity
public class Dataset {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String path;

    private String source;

    @Builder.Default
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "dataset_id")
    private Set<DatasetColumn> columns = new HashSet<>();

    public void addColumn(String name, DataType dataType) {
        DatasetColumn datasetColumn = DatasetColumn.builder()
                .name(name)
                .dataType(dataType)
                .dataset(this)
                .build();
        columns.add(datasetColumn);
    }
}
