package entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id")
    private int projectId;

    @Column(name = "project_name", length = 100)
    private String projectname;

    @Column(name = "project_status", length = 100)
    private String projectstatus;

    @Column(name = "project_start")
    private Date projectstart;

    @Column(name = "project_end")
    private Date projectend;

    @Column(name = "client_name", length = 100)
    private String clientname;

    @Column(name = "contract_amount")
    private double contractamount;

    @Column(name = "downpayment")
    private double downpayment;

    @JsonProperty("companyname")
    @Column(name = "company_name", length = 100)
    private String companyname;

    @JsonProperty("companyLocation")
    @Column(name = "company_location", length = 100)
    private String companyLocation;

    @JsonProperty("companycontact")
    @Column(name = "company_contact", length = 100)
    private String companycontact;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    @JsonIgnore // 🔥 This ignores it for JSON deserialization (fixes 415)
    private List<ProjectInventory> projectInventoryList;

}