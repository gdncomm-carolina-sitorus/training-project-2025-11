package com.marketplace.member.exception;

public class EmailAlreadyExistsException extends RuntimeException {
  public EmailAlreadyExistsException(String msg) {
    super(msg);
  }
}
