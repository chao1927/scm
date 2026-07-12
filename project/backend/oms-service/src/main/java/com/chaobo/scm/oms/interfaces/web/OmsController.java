package com.chaobo.scm.oms.interfaces.web;

import com.chaobo.scm.oms.application.OmsApplicationService;
import com.chaobo.scm.oms.infrastructure.persistence.OmsMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/oms/v1")
public class OmsController {
    private final OmsApplicationService service;

    public OmsController(OmsApplicationService service) {
        this.service = service;
    }

    @PostMapping("/channel-orders")
    public OmsMapper.SalesOrderRow receive(@RequestBody OmsApplicationService.ReceiveChannelOrder command) {
        return service.receiveChannelOrder(command);
    }

    @GetMapping("/channel-orders")
    public List<OmsMapper.ChannelOrderRow> channelOrders() {
        return service.listChannelOrders();
    }

    @GetMapping("/sales-orders")
    public List<OmsMapper.SalesOrderRow> orders() {
        return service.listOrders();
    }

    @GetMapping("/sales-orders/{orderNo}")
    public OmsMapper.SalesOrderRow order(@PathVariable String orderNo) {
        return service.getOrder(orderNo);
    }

    @PostMapping("/sales-orders/{orderNo}/review")
    public OmsMapper.SalesOrderRow review(@PathVariable String orderNo, @RequestBody OmsApplicationService.ReviewCommand command) {
        return service.reviewSalesOrder(orderNo, command);
    }
}
