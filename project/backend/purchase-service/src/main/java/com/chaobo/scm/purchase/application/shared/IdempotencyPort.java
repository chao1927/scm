package com.chaobo.scm.purchase.application.shared;

import java.util.Optional;
import java.util.function.Supplier;

public interface IdempotencyPort {

    Optional<CommandResult> find(String businessType, String idempotencyKey);

    CommandResult execute(String businessType, CommandContext context, Supplier<CommandResult> action);
}
