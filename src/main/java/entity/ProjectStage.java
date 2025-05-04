package entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
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

    @JsonProperty("stageNumber")
    @Column(name = "stage_number")
    private Integer stageNumber;

    @ManyToOne
    @JoinColumn(name = "project_id")
    @JsonIgnore  // Prevent recursion
    private Project project;

    @OneToMany(mappedBy = "stage", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("stage") // Prevent infinite loop
    private List<ProjectTask> tasks = new ArrayList<>();

    @Column(name = "status")
    @JsonProperty("status")
    private String status;

    // ✅ Add this field to represent the relationship with ProjectInventory
    @OneToMany(mappedBy = "stage", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("stage") // Prevent infinite loop
    private List<ProjectInventory> projectInventoryList = new ArrayList<>();
}
