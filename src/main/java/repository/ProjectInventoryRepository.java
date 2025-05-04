package repository;

import entity.Inventory;
import entity.Project;
import entity.ProjectInventory;
import entity.ProjectStage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectInventoryRepository extends JpaRepository<ProjectInventory, Long> {
    ProjectInventory findByProjectAndInventory(Project project, Inventory inventory);
    List<ProjectInventory> findByProject_ProjectIdIn(List<Integer> projectIds);
    List<ProjectStage> findByProject(Project project);
    // ProjectInventoryRepository.java
    ProjectInventory findByProjectAndInventoryAndStage(Project project, Inventory inventory, ProjectStage stage);

}
