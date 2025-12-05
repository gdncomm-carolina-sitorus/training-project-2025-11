package com.marketplace.cart.exception;

public class ProductNotFoundInCartException extends RuntimeException {
  public ProductNotFoundInCartException(String message) {
    super(message);
  }
}
