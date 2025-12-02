package com.wijaya.commerce.product.command;

public interface CommandExecutor {

    <R, T> T execute(Class<? extends Command<R, T>> commandClass, R request);
}
