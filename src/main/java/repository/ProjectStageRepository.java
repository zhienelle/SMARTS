package repository;

import entity.ProjectStage;
import entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectStageRepository extends JpaRepository<ProjectStage, Integer> {
    List<ProjectStage> findByProject(Project project);

    Optional<ProjectStage> findByProjectAndStageNumber(Project project, int stageNumber);

}
