package com.chaobo.scm.purchase.domain.shared;

public interface IdentifierGenerator {

    long nextId();

    String nextCode(String prefix);
}
