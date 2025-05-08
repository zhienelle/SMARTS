package entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import entity.ProjectStage;


@Entity
@Table(name = "material_request")
public class MaterialRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "material_request_id")
    private Long materialRequestId;

    // 🔗 FK to projects
    @ManyToOne
    @JoinColumn(name = "project_id", referencedColumnName = "project_id", nullable = false)
    private Project project;

    // 🔗 FK to inventory
    @ManyToOne
    @JoinColumn(name = "material_id", referencedColumnName = "material_id", nullable = false)
    private Inventory inventory;

    // 🔗 FK to user who requested
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false)
    private User user;

    // 🔗 FK to project stage
    @ManyToOne
    @JoinColumn(name = "stage_id")
    private ProjectStage stage;

    // 📦 Requested quantity
    @Column(name = "material_stock", nullable = false)
    private Integer materialStock;

    // 🕒 Auto-set on creation
    @Column(name = "request_date", nullable = false)
    private LocalDateTime requestDate = LocalDateTime.now();

    // 🔄 Status: PENDING / ACCEPTED / DENIED
    @Column(name = "material_request_status", nullable = false)
    private String materialRequestStatus = "PENDING";

    // 🆕 Material name snapshot (for reporting/auditing)
    @Column(name = "material_name")
    private String materialName;

    // 🆕 Material category snapshot
    @Column(name = "material_category")
    private String materialCategory;



    // --- Constructors ---
    public MaterialRequest() {
        this.requestDate = LocalDateTime.now();
        this.materialRequestStatus = "PENDING";
    }

    public MaterialRequest(Project project, Inventory inventory, Integer materialStock) {
        this.project = project;
        this.inventory = inventory;
        this.materialStock = materialStock;
        this.requestDate = LocalDateTime.now();
        this.materialRequestStatus = "PENDING";
    }

    // --- Getters and Setters ---

    public ProjectStage getStage() {
        return stage;
    }

    public void setStage(ProjectStage stage) {
        this.stage = stage;
    }


    public Long getMaterialRequestId() {
        return materialRequestId;
    }

    public void setMaterialRequestId(Long materialRequestId) {
        this.materialRequestId = materialRequestId;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Integer getMaterialStock() {
        return materialStock;
    }

    public void setMaterialStock(Integer materialStock) {
        this.materialStock = materialStock;
    }

    public LocalDateTime getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDateTime requestDate) {
        this.requestDate = requestDate;
    }

    public String getMaterialRequestStatus() {
        return materialRequestStatus;
    }

    public void setMaterialRequestStatus(String materialRequestStatus) {
        this.materialRequestStatus = materialRequestStatus;
    }

    public String getMaterialName() {
        return materialName;
    }

    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }

    public String getMaterialCategory() {
        return materialCategory;
    }

    public void setMaterialCategory(String materialCategory) {
        this.materialCategory = materialCategory;
    }
}
