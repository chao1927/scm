package org.scm.bdp.service.application.handler;

import lombok.extern.slf4j.Slf4j;
import org.scm.bdp.service.application.command.product.*;
import org.scm.bdp.service.application.converter.ProductCategoryConverter;
import org.scm.bdp.service.application.event.product.*;
import org.scm.bdp.service.domain.model.ProductCategoryAgg;
import org.scm.bdp.service.domain.repository.ProductCategoryRepository;
import org.scm.bdp.service.domain.repository.ProductRepository;
import org.scm.common.EventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProductCategoryCommandHandler {

    @Autowired
    private ProductCategoryRepository productCategoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private EventPublisher eventPublisher;

    public void handle(CreateProductCategoryCommand cmd) {
        // 校验产品分类是否重复
        productCategoryRepository.checkNameExist(cmd.name());
        // 校验父级分类是否存在
        productCategoryRepository.checkExistById(cmd.parentId());

        // 转换聚合
        ProductCategoryAgg agg = ProductCategoryConverter.cmdConvertAgg(cmd);
        productCategoryRepository.save(agg);

        // 发送商品分类创建事件
        eventPublisher.publish(new ProductCategoryCreatedEvent(agg.id()));
    }

    public void handle(UpdateProductCategoryCommand cmd) {
        // 查询聚合
        ProductCategoryAgg agg = productCategoryRepository.findById(cmd.id());

        // 校验名称重复
        productCategoryRepository.checkNameDuplicate(cmd.id(), cmd.name());

        // 校验父级分类是否存在
        productCategoryRepository.checkExistById(cmd.parentId());

        // 更新聚合
        agg.update(cmd.name(), cmd.parentId(), cmd.attributes(), cmd.sortOrder());
        productCategoryRepository.save(agg);

        // 发送商品分类更新事件
        eventPublisher.publish(new ProductCategoryUpdatedEvent(agg.id()));
    }

    public void handle(DisableProductCategoryCommand cmd) {
        ProductCategoryAgg agg = productCategoryRepository.findById(cmd.id());

        // 判断是否该分类下是否有产品
        productRepository.checkExistByCategoryId(cmd.id());

        // 判断是否该分类下是否有子分类
        productCategoryRepository.checkExistByParentId(cmd.id());

        // 禁用
        agg.disable();
        productCategoryRepository.save(agg);

        // 发布商品分类禁用事件
        eventPublisher.publish(new ProductCategoryDisabledEvent(agg.id()));
    }


    public void handle(EnableProductCategoryCommand cmd) {
        // 查询聚合
        ProductCategoryAgg agg = productCategoryRepository.findById(cmd.id());

        // 启用商品分类
        agg.enable();
        productCategoryRepository.save(agg);

        eventPublisher.publish(new ProductCategoryEnabledEvent(agg.id()));
    }

    public void handle(DeleteProductCategoryCommand cmd) {
        // 判断是否该分类下是否有产品
        productRepository.checkExistByCategoryId(cmd.id());

        productCategoryRepository.deleteById(cmd.id());
        eventPublisher.publish(new ProductCategoryDeletedEvent(cmd.id()));
    }
}
