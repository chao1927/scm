package com.chaobo.scm.supplier.domain.shared;

public interface IdentifierGenerator {
    long nextId();

    String nextBusinessNo(String prefix);
}
