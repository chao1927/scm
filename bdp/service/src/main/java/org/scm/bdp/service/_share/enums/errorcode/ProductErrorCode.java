package org.scm.bdp.service._share.enums.errorcode;

import org.scm.common.exception.ErrorCode;

public enum ProductErrorCode implements ErrorCode {


    PRODUCT_NOT_FOUND("P10001", "Product not found"),

    PRODUCT_SKU_EXIST("P10002", "Product name exist"),

    PRODUCT_SKU_DUPLICATE("P10003", "Product name duplicate"),

    PRODUCT_CATEGORY_NOT_FOUND("P10004", "Product category not found"),

    PRODUCT_CATEGORY_NAME_EXIST("P10005", "Product category exist"),

    PRODUCT_CATEGORY_NAME_DUPLICATE("P10006", "Product category duplicate"),

    PRODUCT_CATEGORY_HAS_CHILD("P10007", "Product category has child"),

    CATEGORY_NOT_EXIST("P002", "商品分类不存在"),
    CATEGORY_DISABLED("P003", "商品分类已禁用"),
    DUPLICATE_SKU("P004", "SKU 重复"),
    INVALID_PRICE("P005", "价格不合法"),

    PRODUCT_CATEGORY_EXIST_PRODUCT("P006", "商品分类下存在商品");

    private final String code;

    private final String message;

    ProductErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }


    @Override
    public String getCode() {
        return null;
    }

    @Override
    public String getMessage() {
        return null;
    }
}
