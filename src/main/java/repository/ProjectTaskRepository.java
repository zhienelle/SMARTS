package repository;

import entity.ProjectTask;
import entity.ProjectStage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectTaskRepository extends JpaRepository<ProjectTask, Integer> {
    List<ProjectTask> findByStage(ProjectStage stage);
}
