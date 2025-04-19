package org.scm.srm.wms.adapter.infra.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scm.common.BaseBO;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "purchase_storage")
public class PurchaseStorage extends BaseBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "storage_no", nullable = false, length = 50, unique = true)
    private String storageNo;

    @Column(name = "start_storage_time")
    private LocalDateTime startStorageTime;

    @Column(name = "finish_storage_time")
    private LocalDateTime finishStorageTime;

    @Column(name = "start_inspection_time")
    private LocalDateTime startInspectionTime;

    @Column(name = "finish_inspection_time")
    private LocalDateTime finishInspectionTime;

    @Column(name = "start_shelving_time")
    private LocalDateTime startShelvingTime;

    @Column(name = "finish_shelving_time")
    private LocalDateTime finishShelvingTime;

    @Column(name = "receipt_no", nullable = false, length = 50)
    private String receiptNo;

    @Column(name = "operator_emp_id", nullable = false)
    private Long operatorEmpId;

}
