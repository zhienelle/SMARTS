package repository;

import entity.ProjectInventory;
import entity.ProjectStage;
import entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectStageRepository extends JpaRepository<ProjectStage, Integer> {
    List<ProjectStage> findByProject(Project project);

    Optional<ProjectStage> findByProjectAndStageNumber(Project project, int stageNumber);

    @Query("SELECT COUNT(ps) FROM ProjectStage ps WHERE ps.project.projectId = :projectId AND ps.status <> 'Complete'")
    int countIncompleteStagesByProjectId(@Param("projectId") Long projectId);

    List<ProjectStage> findByProject_Projectname(String projectName);

    void deleteByStageIdIn(List<Integer> stageIds);


}
