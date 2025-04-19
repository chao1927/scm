package org.scm.bdp.service.adapter.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.scm.bdp.service.application.command.product.*;
import org.scm.bdp.service.application.handler.ProductCommandHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductCommandHandler handler;

    /**
     * 创建商品
     */
    @PostMapping
    public ResponseEntity<Void> create(@RequestBody @Valid CreateProductCommand command) {
        handler.handle(command);
        return ResponseEntity.ok().build();
    }

    /**
     * 更新商品
     */
    @PutMapping
    public ResponseEntity<Void> update(@RequestBody @Valid UpdateProductCommand command) {
        handler.handle(command);
        return ResponseEntity.ok().build();
    }

    /**
     * 禁用商品
     */
    @PostMapping("/{id}/disable")
    public ResponseEntity<Void> disable(@PathVariable Long id) {
        DisableProductCommand command = new DisableProductCommand(id);
        handler.handle(command);
        return ResponseEntity.ok().build();
    }

    /**
     * 启用商品
     */
    @PostMapping("/{id}/enable")
    public ResponseEntity<Void> enable(@PathVariable Long id) {
        EnableProductCommand command = new EnableProductCommand(id);
        handler.handle(command);
        return ResponseEntity.ok().build();
    }

    /**
     * 删除商品
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        DeleteProductCommand command = new DeleteProductCommand(id);
        handler.handle(command);
        return ResponseEntity.ok().build();
    }
}
