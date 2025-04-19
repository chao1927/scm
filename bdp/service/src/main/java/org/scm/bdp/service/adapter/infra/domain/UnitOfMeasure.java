package org.scm.bdp.service.adapter.infra.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.scm.bdp.service._share.enums.SwitchStatus;
import org.scm.common.BaseBO;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "unit_of_measure")
public class UnitOfMeasure extends BaseBO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id; // 单位ID

    @Column(name = "name", nullable = false, length = 20, unique = true)
    private String name; // 单位名称

    @Column(name = "status", nullable = false)
    private Integer status;

    public void enable() {
        this.status = SwitchStatus.ENABLED.getValue();
    }

    public void disable() {
        this.status = SwitchStatus.DISABLED.getValue();
    }


}
