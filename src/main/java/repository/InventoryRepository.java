package repository;

import entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Integer> {
    Inventory findByMaterialCategoryAndMaterialName(String materialCategory, String materialName);
    Inventory findByMaterialName(String materialName);
    Inventory findByMaterialCategoryIgnoreCaseAndMaterialNameIgnoreCase(String category, String name);

}
