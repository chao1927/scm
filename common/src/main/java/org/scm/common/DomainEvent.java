package org.scm.common;

public interface DomainEvent {

    String topic();

    String type();

}
