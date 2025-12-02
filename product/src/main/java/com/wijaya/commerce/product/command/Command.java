package com.wijaya.commerce.product.command;

public interface Command<R, T> {
    T doCommand(R commandRequest);

}
