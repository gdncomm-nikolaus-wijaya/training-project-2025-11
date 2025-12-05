package com.wijaya.commerce.cart.command;

public interface Command<R, T> {
    T doCommand(R request);
}
