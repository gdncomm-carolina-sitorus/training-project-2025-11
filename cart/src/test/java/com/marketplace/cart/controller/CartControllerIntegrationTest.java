package com.marketplace.cart.controller;

import com.marketplace.cart.model.ApiResponse;
import com.marketplace.cart.model.Cart;
import com.marketplace.cart.model.CartItem;
import com.marketplace.cart.service.CartService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebFluxTest(CartController.class)
class CartControllerIntegrationTest {

  @Autowired
  private WebTestClient webTestClient;

  @MockitoBean
  private CartService cartService;

  @Test
  void testGetCart_Success() {
    Cart cart = new Cart();
    cart.setCustomerId("user1");

    when(cartService.getCartWithDetails("user1")).thenReturn(Mono.just(cart));

    webTestClient.get()
        .uri("/api/cart")
        .header("X-User-Id", "user1")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.success")
        .isEqualTo(true)
        .jsonPath("$.message")
        .isEqualTo("Cart retrieved successfully")
        .jsonPath("$.data.customerId")
        .isEqualTo("user1");
  }

  @Test
  void testGetCart_Empty() {
    when(cartService.getCartWithDetails("user2")).thenReturn(Mono.empty());

    webTestClient.get()
        .uri("/api/cart")
        .header("X-User-Id", "user2")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.success")
        .isEqualTo(true)
        .jsonPath("$.message")
        .isEqualTo("Cart is empty");
  }

  @Test
  void testGetCart_Error() {
    when(cartService.getCartWithDetails("user3")).thenReturn(Mono.error(new RuntimeException(
        "Cart not found")));

    webTestClient.get()
        .uri("/api/cart")
        .header("X-User-Id", "user3")
        .exchange()
        .expectStatus()
        .isNotFound()
        .expectBody()
        .jsonPath("$.success")
        .isEqualTo(false)
        .jsonPath("$.message")
        .isEqualTo("Cart not found");
  }

  @Test
  void testAddItem_Success() {
    CartItem item = new CartItem();
    item.setProductId("p1");
    item.setQuantity(2);

    Cart cart = new Cart();
    cart.setCustomerId("user1");

    when(cartService.addItem(eq("user1"), any(CartItem.class))).thenReturn(Mono.just(cart));

    webTestClient.post()
        .uri("/api/cart/add")
        .header("X-User-Id", "user1")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(item)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.success")
        .isEqualTo(true)
        .jsonPath("$.message")
        .isEqualTo("Item added to cart successfully");
  }

  @Test
  void testRemoveItem_Success() {
    Cart cart = new Cart();
    cart.setCustomerId("user1");

    when(cartService.removeItem("user1", "p1")).thenReturn(Mono.just(ApiResponse.<Cart>builder()
        .success(true)
        .message("Item removed")
        .data(cart)
        .build()));

    webTestClient.delete()
        .uri("/api/cart/remove/p1")
        .header("X-User-Id", "user1")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.success")
        .isEqualTo(true);
  }
}
