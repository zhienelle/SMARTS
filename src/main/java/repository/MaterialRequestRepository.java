package repository;

import entity.MaterialRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List; // ✅ Add this

public interface MaterialRequestRepository extends JpaRepository<MaterialRequest, Long> {
    List<MaterialRequest> findByMaterialRequestStatus(String status);
}
