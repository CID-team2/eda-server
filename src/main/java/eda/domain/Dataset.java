package eda.domain;

import lombok.Getter;

import javax.persistence.*;
import java.util.Set;

@Getter
@Entity
public class Dataset extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String path;

    private String source;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "dataset_id")
    private Set<DatasetColumn> columns;
}
