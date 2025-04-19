package org.scm.tms.adapter.infra.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scm.common.BaseBO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "logistics_order")
public class LogisticsOrder extends BaseBO {

    // 主键单独声明
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "logistics_no", nullable = false, unique = true, length = 50)
    private String logisticsNo;
    
    @Column(name = "related_customer_id")
    private Long relatedCustomerId;
    
    // 发货信息
    @Column(name = "sender_name", length = 50)
    private String senderName;
    
    @Column(name = "sender_phone", length = 20)
    private String senderPhone;
    
    @Column(name = "sender_address", length = 200)
    private String senderAddress;
    
    @Column(name = "sender_postcode", length = 10)
    private String senderPostcode;
    
    @Column(name = "sender_longitude", precision = 10, scale = 6)
    private BigDecimal senderLongitude;
    
    @Column(name = "sender_latitude", precision = 10, scale = 6)
    private BigDecimal senderLatitude;
    
    // 收货信息
    @Column(name = "receiver_name", length = 50)
    private String receiverName;
    
    @Column(name = "receiver_phone", length = 20)
    private String receiverPhone;
    
    @Column(name = "receiver_address", length = 200)
    private String receiverAddress;
    
    @Column(name = "receiver_postcode", length = 10)
    private String receiverPostcode;
    
    @Column(name = "receiver_longitude", precision = 10, scale = 6)
    private BigDecimal receiverLongitude;
    
    @Column(name = "receiver_latitude", precision = 10, scale = 6)
    private BigDecimal receiverLatitude;
    
    // 货物信息
    @Column(name = "goods_name", length = 100)
    private String goodsName;
    
    @Column(name = "goods_type", length = 50)
    private String goodsType;
    
    @Column(name = "goods_quantity")
    private Integer goodsQuantity;
    
    @Column(name = "goods_weight", precision = 10, scale = 2)
    private BigDecimal goodsWeight;
    
    @Column(name = "logistic_cost", precision = 10, scale = 2)
    private BigDecimal logisticCost;
    
    @Column(name = "logistic_status", nullable = false)
    private Integer logisticStatus;  // 例如：1-待揽收,2-运输中,3-已签收,4-异常
    
    @Column(name = "logistic_type", nullable = false)
    private Integer logisticType;    // 如 1-次日达,2-隔日达,3-闪送,4-冷链,5-快运,6-普运
    
    @Column(name = "pickup_time")
    private LocalDateTime pickupTime;
    
    @Column(name = "send_time")
    private LocalDateTime sendTime;
    
    @Column(name = "delivery_time")
    private LocalDateTime deliveryTime;
    
    @Column(name = "delivery_details", columnDefinition = "TEXT")
    private String deliveryDetails;
    
    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

}
