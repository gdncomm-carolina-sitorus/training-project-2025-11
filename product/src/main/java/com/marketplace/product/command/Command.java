package com.marketplace.product.command;

public interface Command<R, T> {
    R execute(T request);
}
