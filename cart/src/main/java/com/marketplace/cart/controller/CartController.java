package com.marketplace.cart.controller;

import com.marketplace.cart.exception.ProductNotFoundInCartException;
import com.marketplace.cart.model.ApiResponse;
import com.marketplace.cart.model.Cart;
import com.marketplace.cart.model.CartItem;
import com.marketplace.cart.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
  public Mono<ResponseEntity<ApiResponse<Cart>>> getCart(@RequestHeader("X-User-Id") String customerId) {
    return cartService.getCartWithDetails(customerId)
        .map(cart -> ResponseEntity.ok(ApiResponse.<Cart>builder()
            .success(true)
            .message("Cart retrieved successfully")
            .data(cart)
            .build()))
        .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.OK)
            .body(ApiResponse.<Cart>builder()
                .success(true)
                .message("Cart is empty")
                .data(null)
                .build())))
        .onErrorResume(ex -> Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.<Cart>builder()
                .success(false)
                .message(ex.getMessage())
                .data(null)
                .build())));
  }

  @PostMapping("/add")
  public Mono<ResponseEntity<ApiResponse<Cart>>> addItem(@RequestHeader("X-User-Id") String customerId,
      @Valid @RequestBody CartItem item) {
    return cartService.addItem(customerId, item)
        .map(cart -> ResponseEntity.ok(ApiResponse.<Cart>builder()
            .success(true)
            .message("Item added to cart successfully")
            .data(null)
            .build()))
        .onErrorResume(ex -> Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.<Cart>builder()
                .success(false)
                .message(ex.getMessage())
                .data(null)
                .build())));
  }

  @DeleteMapping("/remove/{productId}")
  public Mono<ResponseEntity<ApiResponse<Cart>>> removeItem(@RequestHeader("X-User-Id") String customerId,
      @PathVariable String productId) {
    return cartService.removeItem(customerId, productId).map(ResponseEntity::ok);
  }

  @DeleteMapping("/clear")
  public Mono<ResponseEntity<ApiResponse<Void>>> clearCart(@RequestHeader("X-User-Id") String customerId) {
    return cartService.clearCart(customerId)
        .map(cart -> ResponseEntity.ok(ApiResponse.<Void>builder()
            .success(true)
            .message("Cart cleared successfully")
            .build()));
  }
}
