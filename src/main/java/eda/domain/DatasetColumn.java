package eda.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class DatasetColumn {
    @Column(name = "_index")
    private int index;
    private String name;
    private String dataType;
}
