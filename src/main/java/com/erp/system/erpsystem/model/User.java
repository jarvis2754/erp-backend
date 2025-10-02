package com.erp.system.erpsystem.model;

import com.erp.system.erpsystem.model.enums.ActiveStatus;
import com.erp.system.erpsystem.model.enums.Department;
import com.erp.system.erpsystem.model.enums.Gender;
import com.erp.system.erpsystem.model.enums.Position;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name= "\"user\"")
public class User {
    //    int userId;
    //    String uuId;
    //    String userName;
    //    String email;
    //    String phoneNumber;
    //    Department department;
    //    Position reportingManager;
    //    LocalDate joiningDate;
    //    String password;
    //    Position position;
    //    String orgId;
   // List<User> teamMembers;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;

    @Column(nullable = false, unique = true)
    private String uuId;

    @Column(nullable = false)
    private String userName;

    @Column(unique = true, nullable = false)
    private String email;

    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    private Department department;

    @ManyToOne
    @JoinColumn(name = "reporting_manager_id")
    private User reportingManager;

    @OneToMany(mappedBy = "reportingManager")
    private List<User> teamMembers;

    @Column(name = "joining_date", updatable = false)
    private LocalDate joiningDate;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Position position;

    @Enumerated(EnumType.STRING)
    private ActiveStatus status =ActiveStatus.ACTIVE;

    @ManyToOne
    @JoinColumn(name = "organization_id", foreignKey = @ForeignKey(name = "fk_user_org"))
    private Organization organization;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attendance> attendances;



}
