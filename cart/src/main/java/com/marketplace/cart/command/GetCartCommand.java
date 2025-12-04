package com.marketplace.cart.command;

import com.marketplace.cart.model.Cart;
import com.marketplace.cart.model.GetCartRequest;
import com.marketplace.cart.repository.CartRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
@AllArgsConstructor
public class GetCartCommand implements Command<Cart, GetCartRequest> {

  private final CartRepository cartRepository;

  @Override
  public Mono<Cart> execute(GetCartRequest request) {
    String customerId = request.getCustomerId();

    return Mono.fromCallable(() -> cartRepository.findById(customerId))
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(optionalCart -> optionalCart.map(Mono::just).orElseGet(Mono::empty));
  }
}
