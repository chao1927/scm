package com.chaobo.scm.purchase.infrastructure.persistence.event;

import com.chaobo.scm.purchase.domain.shared.IdentifierGenerator;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class SnowflakeLikeIdentifierGenerator implements IdentifierGenerator {
    private final AtomicLong sequence = new AtomicLong(System.currentTimeMillis());

    @Override
    public long nextId() {
        return sequence.incrementAndGet();
    }

    @Override
    public String nextCode(String prefix) {
        var date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        return prefix + date + sequence.incrementAndGet();
    }
}
