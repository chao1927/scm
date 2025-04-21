package org.scm.bdp.service.application.command.rbac;

public record ChangePasswordCommand(Long id, String oldPassword, String newPassword) {

}
