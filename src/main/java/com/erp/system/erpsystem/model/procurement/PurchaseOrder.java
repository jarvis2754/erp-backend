package com.erp.system.erpsystem.model.procurement;

import com.erp.system.erpsystem.model.Organization;
import com.erp.system.erpsystem.model.enums.POStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "purchase_order")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer poId;

    private BigDecimal totalPrice;
    private String taxes;
    private String deliveryTerms;

    @Enumerated(EnumType.STRING)
    private POStatus status = POStatus.OPEN;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "vendor_id")
    private Vendor vendor;

    @ManyToOne
    @JoinColumn(name = "pr_id")
    private PurchaseRequisition purchaseRequisition;

    @ManyToOne
    @JoinColumn(name = "org_id")
    private Organization org;

}

