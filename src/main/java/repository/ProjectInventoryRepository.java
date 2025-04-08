package repository;

import entity.Inventory;
import entity.Project;
import entity.ProjectInventory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectInventoryRepository extends JpaRepository<ProjectInventory, Long> {
    ProjectInventory findByProjectAndInventory(Project project, Inventory inventory);
}
