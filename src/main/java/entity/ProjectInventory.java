package entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "project_inventory")
public class ProjectInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_inventory_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "project_id")
    @JsonBackReference
    @JsonIgnore
    private Project project;


    @ManyToOne
    @JoinColumn(name = "material_id", referencedColumnName = "material_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // ✅ Add this
    private Inventory inventory;


    @Column(name = "quantity_assigned", nullable = false)
    private Integer quantityAssigned;

    @Column(name = "quantity_used", nullable = false)
    private Integer quantityUsed = 0;

    @Column(name = "material_price")
    private double materialPrice;

    @Column(name = "total_price")
    private double totalPrice;


    // --- Getters and Setters ---
    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
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

    public Integer getQuantityAssigned() {
        return quantityAssigned;
    }

    public void setQuantityAssigned(Integer quantityAssigned) {
        this.quantityAssigned = quantityAssigned;
    }

    public Integer getQuantityUsed() {
        return quantityUsed;
    }

    public void setQuantityUsed(Integer quantityUsed) {
        this.quantityUsed = quantityUsed;
    }

    public double getMaterialPrice() {
        return materialPrice;
    }

    public void setMaterialPrice(double materialPrice) {
        this.materialPrice = materialPrice;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }



}
