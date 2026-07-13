package com.chaobo.scm.mdm.interfaces.web;

import com.chaobo.scm.mdm.application.MdmPublicationApplicationService;
import com.chaobo.scm.mdm.infrastructure.persistence.MdmPublicationMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/mdm/v1")
public class MdmPublicationController {
    private final MdmPublicationApplicationService service;

    public MdmPublicationController(MdmPublicationApplicationService service) {
        this.service = service;
    }

    @PostMapping("/publication-subscriptions")
    public MdmPublicationMapper.SubscriptionRow createSubscription(@RequestBody MdmPublicationApplicationService.CreateSubscriptionCommand command) {
        return service.createSubscription(command);
    }

    @PostMapping("/publication-subscriptions/{subscriptionNo}/disable")
    public MdmPublicationMapper.SubscriptionRow disableSubscription(@PathVariable String subscriptionNo,
                                                                    @RequestBody MdmPublicationApplicationService.DisableSubscriptionCommand command) {
        return service.disableSubscription(subscriptionNo, command);
    }

    @GetMapping("/publication-subscriptions")
    public List<MdmPublicationMapper.SubscriptionRow> subscriptions() {
        return service.listSubscriptions();
    }

    @PostMapping("/publications")
    public List<MdmPublicationMapper.PublicationRow> publish(@RequestBody MdmPublicationApplicationService.PublishCommand command) {
        return service.publish(command);
    }

    @PostMapping("/publications/{publicationNo}/retry")
    public MdmPublicationMapper.PublicationRow retry(@PathVariable String publicationNo,
                                                    @RequestBody MdmPublicationApplicationService.RetryCommand command) {
        return service.retry(publicationNo, command);
    }

    @GetMapping("/publications")
    public List<MdmPublicationMapper.PublicationRow> publications() {
        return service.listPublications();
    }
}
