package com.marketplace.cart.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "carts")
public class Cart {
  @Id
  private String customerId; // Using customerId as the primary key
  private List<CartItem> items = new ArrayList<>();

  @Transient
  private MemberDetail customer;

  @Transient
  private java.math.BigDecimal totalPrice;
}
