package entity;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "project_task")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id")
    @JsonProperty("task_id")
    private Integer taskId;

    @Column(name = "task_name", length = 255)
    @JsonProperty("task_name")
    private String taskName;

    @Column(name = "completed")
    @JsonProperty("completed")
    private boolean completed;

    @ManyToOne
    @JoinColumn(name = "stage_id")
    @JsonProperty("stage")
    private ProjectStage stage;
}
