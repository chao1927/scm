ge org.scm.bdp.service.application.handler;

import org.scm.bdp.service.application.command.product.*;
import org.scm.bdp.service.application.converter.ProductConverter;
import org.scm.bdp.service.application.event.product.*;
import org.scm.bdp.service.domain.model.ProductAgg;
import org.scm.bdp.service.domain.repository.ProductCategoryRepository;
import org.scm.bdp.service.domain.repository.ProductRepository;
import org.scm.bdp.service.domain.repository.UnitOfMeasureRepository;
import org.scm.common.EventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProductCommandHandler {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductCategoryRepository productCategoryRepository;

    @Autowired
    private UnitOfMeasureRepository unitOfMeasureRepository;

    @Autowired
    private EventPublisher eventPublisher;

    public void handle(CreateProductCommand command) {
        // 校验产品分类是否存在
        productCategoryRepository.checkExistById(command.categoryId());

        // 校验计量单位是否存在
        unitOfMeasureRepository.checkExistById(command.unitId());

        // 转换聚合
        ProductAgg productAgg = ProductConverter.cmdConvertAgg(command);
        productAgg.initSpu();
        productAgg.initSku();
        productRepository.save(productAgg);

        // 发送产品创建事件
        eventPublisher.publish(new ProductCreatedEvent(productAgg.id()));
    }

    public void handle(UpdateProductCommand command) {
        // 校验聚合是否存在
        ProductAgg productAgg = productRepository.findById(command.id());

        // 校验产品分类是否存在
        productCategoryRepository.checkExistById(command.categoryId());

        // 校验计量单位是否存在
        unitOfMeasureRepository.checkExistById(command.unitId());

        // 更新聚合
        productAgg.update(command.name(), command.description(), command.categoryId(), command.unitId());
        productAgg.updateAttributes(command.keyAttributes(), command.salesAttributes());
        productAgg.updatePrice(command.referencePurchasePrice(), command.referenceSalesPrice());
        productRepository.save(productAgg);

        // 发送产品更新事件
        eventPublisher.publish(new ProductUpdatedEvent(productAgg.id()));
    }

    public void handle(DisableProductCommand command) {
        // 校验聚合是否存在
        ProductAgg productAgg = productRepository.findById(command.id());

        // 禁用聚合
        productAgg.disable();
        productRepository.save(productAgg);

        // 发送产品禁用事件
        eventPublisher.publish(new ProductDisabledEvent(productAgg.id()));
    }

    public void handle(EnableProductCommand command) {
        // 校验聚合是否存在
        ProductAgg product = productRepository.findById(command.id());

        // 启用聚合
        product.enable();
        productRepository.save(product);

        // 发送产品启用事件
        eventPublisher.publish(new ProductEnabledEvent(product.product().getId()));
    }

    public void handle(DeleteProductCommand command) {
        // 校验聚合是否存在
        productRepository.checkExistById(command.id());
        productRepository.deleteById(command.id());

        // 发送产品删除事件
        eventPublisher.publish(new ProductDeletedEvent(command.id()));
    }
}
