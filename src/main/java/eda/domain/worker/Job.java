package eda.domain.worker;

import eda.domain.BaseTimeEntity;
import eda.domain.DatasetColumn;
import lombok.*;

import javax.persistence.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Entity
public class Job extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private DatasetColumn column;

    @Setter
    private Status status;

    public Job(DatasetColumn column) {
        this.column = column;
        this.status = Status.WAITING;
    }

    public enum Status {
        WAITING,
        PROCESSING
    }
}
