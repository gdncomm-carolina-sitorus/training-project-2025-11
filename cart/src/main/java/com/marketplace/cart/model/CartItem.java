package com.marketplace.cart.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Transient;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
  public CartItem(String productId, int quantity) {
    this.productId = productId;
    this.quantity = quantity;
  }

  private String productId;
  private int quantity;

  @Transient
  private ProductDetail product;

  @Transient
  private java.math.BigDecimal subtotal;
}
