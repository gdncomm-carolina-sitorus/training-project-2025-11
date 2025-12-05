package com.marketplace.cart.command;

import reactor.core.publisher.Mono;

public interface Command<R, T> {
  Mono<R> execute(T request);
}
