package com.wijaya.commerce.cart.commandImpl;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.wijaya.commerce.cart.command.Command;
import com.wijaya.commerce.cart.command.CommandExecutor;

@Component
public class DefaultExecutor implements CommandExecutor, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public <R, T> T execute(Class<? extends Command<R, T>> commandClass, R request) {
        return this.applicationContext.getBean(commandClass).doCommand(request);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
