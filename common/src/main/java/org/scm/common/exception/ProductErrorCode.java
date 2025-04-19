package org.scm.common.exception;

public enum ProductErrorCode implements ErrorCode {

    PRODUCT_NOT_FOUND("P001", "商品不存在"),
    CATEGORY_NOT_EXIST("P002", "商品分类不存在"),
    CATEGORY_DISABLED("P003", "商品分类已禁用"),
    DUPLICATE_SKU("P004", "SKU 重复"),
    INVALID_PRICE("P005", "价格不合法");

    private final String code;
    private final String message;

    ProductErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
