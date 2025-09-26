package com.erp.system.erpsystem.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "organization")
public class Organization {

//    Integer orgId;
//    String orgCode;
//    String orgName;
//    String phoneNumber;
//    String email;
//    String country;
//    LocalDate createdAt;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer orgId;

    @Column(unique = true,nullable = false)
    private String orgCode;

    @Column(nullable = false)
    private String orgName;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(unique = true, nullable = false)
    private String email;

    private String country;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "owner_id", foreignKey = @ForeignKey(name = "fk_org_owner"))
    private User owner;

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL)
    private List<User> users;

}
