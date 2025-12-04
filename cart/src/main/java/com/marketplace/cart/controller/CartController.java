package com.marketplace.cart.controller;

import com.marketplace.cart.model.ApiResponse;
import com.marketplace.cart.model.Cart;
import com.marketplace.cart.model.CartItem;
import com.marketplace.cart.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/cart")
public class CartController {

  private final CartService cartService;

  public CartController(CartService cartService) {
    this.cartService = cartService;
  }

  @GetMapping
  public reactor.core.publisher.Mono<ResponseEntity<ApiResponse<Cart>>> getCart(@RequestHeader("X-User-Id") String customerId) {
    return cartService.getEnrichedCart(customerId)
        .map(cart -> ResponseEntity.ok(ApiResponse.<Cart>builder()
            .success(true)
            .message("Cart retrieved successfully")
            .data(cart)
            .build()));
  }

  @PostMapping("/add")
  public ResponseEntity<ApiResponse<Cart>> addItem(@RequestHeader("X-User-Id") String customerId,
      @Valid @RequestBody CartItem item) {
    Cart cart = cartService.addItem(customerId, item);
    return ResponseEntity.ok(ApiResponse.<Cart>builder()
        .success(true)
        .message("Item added to cart successfully")
        .build());
  }

  @DeleteMapping("/remove/{productId}")
  public ResponseEntity<ApiResponse<Cart>> removeItem(@RequestHeader("X-User-Id") String customerId,
      @PathVariable String productId) {
    Cart cart = cartService.removeItem(customerId, productId);
    return ResponseEntity.ok(ApiResponse.<Cart>builder()
        .success(true)
        .message("Item removed from cart successfully")
        .build());
  }

  @DeleteMapping("/clear")
  public ResponseEntity<ApiResponse<Void>> clearCart(@RequestHeader("X-User-Id") String customerId) {
    cartService.clearCart(customerId);
    return ResponseEntity.ok(ApiResponse.<Void>builder()
        .success(true)
        .message("Cart cleared successfully")
        .build());
  }
}
