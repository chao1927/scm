package org.scm.srm.wms.adapter.infra.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.scm.common.BaseBO;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sorting_order")
public class SortingOrder extends BaseBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sorting_no", nullable = false, length = 50, unique = true)
    private String sortingNo;

    @Column(name = "wave_no", nullable = false, length = 50)
    private String waveNo; // 对应 wave_order.wave_no

    @Column(name = "sorting_person", nullable = false)
    private Long sortingPerson; // 分拣人（员工ID）

    @Column(name = "sorting_area", length = 100)
    private String sortingArea;

    @Column(name = "sorting_vehicle_no", length = 50)
    private String sortingVehicleNo;

    @Column(name = "picking_start_time")
    private LocalDateTime pickingStartTime;

    @Column(name = "picking_complete_time")
    private LocalDateTime pickingCompleteTime;

    @Column(name = "sorting_status", nullable = false)
    private Integer sortingStatus; // 如1-待拣货,2-拣货中,3-拣货完成,4-异常

    @Column(name = "suggested_route", columnDefinition = "TEXT")
    private String suggestedRoute;

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

}
