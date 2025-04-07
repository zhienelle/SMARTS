package entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "project_stage")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectStage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stage_id")
    @JsonProperty("stage_id")
    private Integer stageId;

    @Column(name = "stage_number")
    @JsonProperty("stage_number")
    private int stageNumber;

    @ManyToOne
    @JoinColumn(name = "project_id")
    @JsonProperty("project")
    private Project project;

    @OneToMany(mappedBy = "stage", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonProperty("tasks")
    private List<ProjectTask> tasks;
}