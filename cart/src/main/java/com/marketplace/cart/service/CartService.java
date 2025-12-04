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
import java.util.Objects;

@Service
@AllArgsConstructor
public class CartService {

  private final AddItemToCartCommand addItemCommand;
  private final RemoveItemFromCartCommand removeItemCommand;
  private final GetCartCommand getCartCommand;
  private final ProductClient productClient;
  private final MemberClient memberClient;

  public Cart getCart(String customerId) {
    return getCartCommand.execute(new GetCartRequest(customerId));
  }

  public reactor.core.publisher.Mono<Cart> getEnrichedCart(String customerId) {
    Cart cart = getCart(customerId);

    reactor.core.publisher.Mono<MemberDetail> memberMono = memberClient.getMemberById(customerId)
        .defaultIfEmpty(MemberDetail.builder().id(Long.parseLong(customerId)).build());

    reactor.core.publisher.Flux<CartItem> itemsFlux =
        reactor.core.publisher.Flux.fromIterable(cart.getItems())
            .flatMap(item -> productClient.getProductById(item.getProductId()).map(product -> {
              item.setProduct(product);
              item.setSubtotal(product.getPrice()
                  .multiply(java.math.BigDecimal.valueOf(item.getQuantity())));
              return item;
            }).defaultIfEmpty(item));

    return reactor.core.publisher.Mono.zip(memberMono, itemsFlux.collectList(), (member, items) -> {
      cart.setCustomer(member);
      cart.setItems(items);
      java.math.BigDecimal total = items.stream()
          .map(i -> i.getSubtotal() != null ? i.getSubtotal() : java.math.BigDecimal.ZERO)
          .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
      cart.setTotalPrice(total);
      return cart;
    });
  }

  public Mono<Cart> addItem(String customerId, CartItem item) {
    Cart cart = addItemCommand.execute(new AddItemRequest(customerId, item));

    Flux<CartItem> itemsFlux = Flux.fromIterable(cart.getItems())
        .flatMap(cartItem -> productClient.getProductById(cartItem.getProductId()).map(product -> {
          cartItem.setProduct(product);
          cartItem.setSubtotal(product.getPrice()
              .multiply(BigDecimal.valueOf(cartItem.getQuantity())));
          return cartItem;
        }).defaultIfEmpty(cartItem));

    Mono<List<CartItem>> enrichedItemsMono = itemsFlux.collectList();

    Mono<MemberDetail> memberMono = memberClient.getMemberById(customerId)
        .defaultIfEmpty(MemberDetail.builder().id(Long.parseLong(customerId)).build());

    return Mono.zip(enrichedItemsMono, memberMono).map(tuple -> {
      List<CartItem> enrichedItems = tuple.getT1();
      MemberDetail member = tuple.getT2();

      cart.setItems(enrichedItems);
      cart.setCustomer(member);

      BigDecimal totalPrice = enrichedItems.stream()
          .map(CartItem::getSubtotal)
          .filter(Objects::nonNull)
          .reduce(BigDecimal.ZERO, BigDecimal::add);

      cart.setTotalPrice(totalPrice);

      return cart;
    });
  }

  public Cart removeItem(String customerId, String productId) {
    return removeItemCommand.execute(new RemoveItemRequest(customerId, productId));
  }

  public void clearCart(String customerId) {
    removeItemCommand.execute(new RemoveItemRequest(customerId, null));
  }
}
