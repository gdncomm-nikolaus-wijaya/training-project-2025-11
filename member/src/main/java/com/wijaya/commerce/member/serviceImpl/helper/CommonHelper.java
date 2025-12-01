package com.wijaya.commerce.member.serviceImpl.helper;

import java.util.Optional;

import org.springframework.beans.BeanUtils;

import com.wijaya.commerce.member.commandImpl.Component;

@Component
public class CommonHelper {
    public static <S, T> T copyProperties(S source, T target,
            String... ignoreProperties) {
        return Optional.ofNullable(source).map(s -> {
            BeanUtils.copyProperties(source, target, ignoreProperties);
            return target;
        }).orElse(null);
    }

}
