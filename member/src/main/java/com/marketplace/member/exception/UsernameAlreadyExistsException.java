package com.marketplace.member.exception;

public class UsernameAlreadyExistsException extends RuntimeException {
  public UsernameAlreadyExistsException(String msg) {
    super(msg);
  }
}
