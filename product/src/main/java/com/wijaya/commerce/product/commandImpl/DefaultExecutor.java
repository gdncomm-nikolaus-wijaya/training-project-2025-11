package com.wijaya.commerce.product.commandImpl;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.wijaya.commerce.product.command.Command;
import com.wijaya.commerce.product.command.CommandExecutor;

@Component
public class DefaultExecutor implements CommandExecutor, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public <R, T> T execute(Class<? extends Command<R, T>> commandClass, R request) {
        return applicationContext.getBean(commandClass).doCommand(request);
    }

    @Override
    public void setApplicationContext(org.springframework.context.ApplicationContext context)
            throws BeansException {
        this.applicationContext = context;
    }

}
