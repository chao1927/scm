package org.scm.bdp.service.adapter.controller;

import jakarta.validation.Valid;
import org.scm.bdp.service.application.command.product.*;
import org.scm.bdp.service.application.handler.ProductCategoryCommandHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/product-categories")
public class ProductCategoryController {

    private ProductCategoryCommandHandler handler;

    /**
     * 创建商品分类
     */
    @PostMapping
    public ResponseEntity<Void> create(@RequestBody @Valid CreateProductCategoryCommand command) {
        handler.handle(command);
        return ResponseEntity.ok().build();
    }

    /**
     * 更新商品分类
     */
    @PutMapping
    public ResponseEntity<Void> update(@RequestBody @Valid UpdateProductCategoryCommand command) {
        handler.handle(command);
        return ResponseEntity.ok().build();
    }

    /**
     * 禁用商品分类
     */
    @PostMapping("/{id}/disable")
    public ResponseEntity<Void> disable(@PathVariable Long id) {
        DisableProductCategoryCommand command = new DisableProductCategoryCommand(id);
        handler.handle(command);
        return ResponseEntity.ok().build();
    }


    @PostMapping("/{id}/enable")
    public ResponseEntity<Void> enable(@PathVariable Long id) {
        EnableProductCategoryCommand command = new EnableProductCategoryCommand(id);
        handler.handle(command);
        return ResponseEntity.ok().build();
    }



    /**
     * 删除商品分类
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        DeleteProductCategoryCommand command = new DeleteProductCategoryCommand(id);
        handler.handle(command);
        return ResponseEntity.ok().build();
    }
}
