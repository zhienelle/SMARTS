package entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "inventory")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "material_id")
    @JsonProperty("material_id")  // matches frontend payload
    private Integer materialId;

    @Column(name = "material_category", length = 100)
    @JsonProperty("material_category")
    private String materialCategory;

    @Column(name = "material_name", length = 100)
    @JsonProperty("material_name")
    private String materialName;

    @Column(name = "material_stock")
    @JsonProperty("material_stock")
    private int materialStock;

    @Column(name = "material_price")
    @JsonProperty("material_price")
    private float materialPrice;

    @Column(name = "material_archived")
    @JsonProperty("material_archived")
    private Boolean materialArchived = false;  // <--New field for archive status // I added =false in this code, remove if it bugs the archive button

    @Column(name = "material_stock_status")
    private String materialStockStatus;

    public void updateStockStatus() {
        if (this.materialStock <= 30) {
            this.materialStockStatus = "LOW";
        } else if (this.materialStock <= 100) {
            this.materialStockStatus = "MODERATE";
        } else {
            this.materialStockStatus = "HIGH";
        }
    }

}
