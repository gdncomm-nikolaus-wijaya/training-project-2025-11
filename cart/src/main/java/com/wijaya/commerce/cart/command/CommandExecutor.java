package com.wijaya.commerce.cart.command;

public interface CommandExecutor {
    <R, T> T execute(Class<? extends Command<R, T>> commandClass, R request);
}
