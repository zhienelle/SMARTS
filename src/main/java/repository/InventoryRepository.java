package repository;

import entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Integer> {
    Inventory findByMaterialCategoryAndMaterialName(String materialCategory, String materialName);
    Inventory findByMaterialName(String materialName);
    Inventory findByMaterialCategoryIgnoreCaseAndMaterialNameIgnoreCase(String category, String name);
    List<Inventory> findByMaterialCategoryIgnoreCase(String materialCategory);
    @Query("SELECT DISTINCT i.materialCategory FROM Inventory i WHERE i.materialCategory IS NOT NULL")
    List<String> findDistinctMaterialCategories();

}
