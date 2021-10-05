package eda.domain;

import javax.persistence.Embeddable;

@Embeddable
public class DatasetColumn {
    private int index;
    private String name;
    private String dataType;
}
