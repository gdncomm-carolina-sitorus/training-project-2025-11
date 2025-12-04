package com.marketplace.cart.service;

import com.marketplace.cart.client.MemberClient;
import com.marketplace.cart.client.ProductClient;
import com.marketplace.cart.command.AddItemToCartCommand;
import com.marketplace.cart.command.GetCartCommand;
import com.marketplace.cart.command.RemoveItemFromCartCommand;
import com.marketplace.cart.model.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

@Service
@AllArgsConstructor
public class CartService {

  private final AddItemToCartCommand addItemCommand;
  private final RemoveItemFromCartCommand removeItemCommand;
  private final GetCartCommand getCartCommand;
  private final ProductClient productClient;
  private final MemberClient memberClient;

  public Mono<Cart> getCart(String customerId) {
    return getCartCommand.execute(new GetCartRequest(customerId));
  }

  public Mono<Cart> getCartWithDetails(String customerId) {
    return getCart(customerId).flatMap(cart -> {
      // If cart has no items, return empty to trigger switchIfEmpty in controller
      if (cart.getItems() == null || cart.getItems().isEmpty()) {
        return Mono.empty();
      }

      Mono<MemberDetail> memberMono = memberClient.getMemberById(customerId);

      Flux<CartItem> itemsFlux = Flux.fromIterable(cart.getItems())
          .flatMap(item -> productClient.getProductById(item.getProductId())
              .switchIfEmpty(Mono.error(new RuntimeException("Product not found: " + item.getProductId())))
              .map(product -> {
                item.setProduct(product);
                item.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                return item;
              })
          );

      return Mono.zip(memberMono, itemsFlux.collectList())
          .map(tuple -> {
            MemberDetail member = tuple.getT1();
            List<CartItem> items = tuple.getT2();

            cart.setCustomer(member);
            cart.setItems(items);

            BigDecimal total = items.stream()
                .map(i -> i.getSubtotal() != null ? i.getSubtotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            cart.setTotalPrice(total);

            return cart;
          });
    });
  }

  public Mono<Cart> addItem(String customerId, CartItem item) {
    return productClient.getProductById(item.getProductId())
        .flatMap(product -> addItemCommand.execute(new AddItemRequest(customerId, item)));
  }

  public Mono<ApiResponse<Cart>> removeItem(String customerId, String productId) {
    return removeItemCommand.execute(new RemoveItemRequest(customerId, productId));
  }

  public Mono<ApiResponse<Cart>> clearCart(String customerId) {
    return removeItemCommand.execute(new RemoveItemRequest(customerId, null));
  }
}
