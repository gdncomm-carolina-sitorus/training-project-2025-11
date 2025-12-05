package com.marketplace.cart.command;

import com.marketplace.cart.model.ApiResponse;
import com.marketplace.cart.model.Cart;
import com.marketplace.cart.model.RemoveItemRequest;
import com.marketplace.cart.repository.CartRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
@AllArgsConstructor
public class RemoveItemFromCartCommand implements Command<ApiResponse<Cart>, RemoveItemRequest> {

  private final CartRepository cartRepository;

  @Override
  public Mono<ApiResponse<Cart>> execute(RemoveItemRequest request) {
    return Mono.fromCallable(() -> {
      Cart cart = cartRepository.findByCustomerId(request.getCustomerId()).orElse(null);

      if (cart == null) {
        return ApiResponse.<Cart>builder().success(false).message("Cart not found").build();
      }

      String productId = request.getProductId();

      if (productId != null && !productId.isBlank()) {
        boolean removed = cart.getItems().removeIf(i -> i.getProductId().equals(productId));
        if (!removed) {
          return ApiResponse.<Cart>builder()
              .success(false)
              .message("Product not exists on cart")
              .build();
        }
      } else {
        cart.getItems().clear();
      }

      cartRepository.save(cart);

      return ApiResponse.<Cart>builder().success(true).message("Item removed successfully").build();
    }).subscribeOn(Schedulers.boundedElastic());
  }
}
