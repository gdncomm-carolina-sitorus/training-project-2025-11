package com.marketplace.member.command;

public interface Command<R, T> {
  R execute(T request);
}
