package com.erp.system.erpsystem.model;

import com.erp.system.erpsystem.model.enums.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "organization")
public class Organization {


//    Integer orgId; pk
//    String orgCode; notnull-ck
//    String orgName;
//    String phoneNumber;
//    String email;
//    String country;
//    String gstVatNumber;
//    String panTinNumber;
//    String taxId;
//    String registeredAddress;
//    String currency;
//    String fiscalYear;
//    String status;
//    LocalDateTime createdAt;
//    User owner;
//    List<User> users;
//    Organization branchOf;
//    List<Organization> branches;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer orgId;
    @Column(unique = true, nullable = false)
    private String orgCode;      // Company Code, e.g., TECH001
    @Column(nullable = false)
    private String orgName;      // Company Name
    private String phoneNumber;
    @Column(unique = true, nullable = false)
    private String email;
    private String country;
    private String gstVatNumber;
    private String panTinNumber;
    private String taxId;
    private String registeredAddress;
    private String currency;
    private String fiscalYear;
    private Status status;
    @CreationTimestamp
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "owner_id", foreignKey = @ForeignKey(name = "fk_org_owner"))
    private User owner;
    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL)
    private List<User> users;

    @ManyToOne
    @JoinColumn(name = "branch_of", foreignKey = @ForeignKey(name = "fk_org_branch"))
    private Organization branchOf;
    @OneToMany(mappedBy = "branchOf", cascade = CascadeType.ALL)
    private List<Organization> branches; // sub-branches of this org

}
