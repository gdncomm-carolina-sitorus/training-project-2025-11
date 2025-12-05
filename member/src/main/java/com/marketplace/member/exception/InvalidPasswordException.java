package com.marketplace.member.exception;

public class InvalidPasswordException extends RuntimeException {
  public InvalidPasswordException() {
    super("Invalid password");
  }
}
