package com.wijaya.commerce.member.serviceImpl.helper;

import org.springframework.stereotype.Service;

import com.wijaya.commerce.member.commandImpl.model.LoginCommandResponse;
import com.wijaya.commerce.member.commandImpl.model.RegisterCommandResponse;
import com.wijaya.commerce.member.restWebModel.response.LoginResponseWebModel;
import com.wijaya.commerce.member.restWebModel.response.RegisterResponseWebModel;

@Service
public class MemberResponseHelper {
    public static RegisterResponseWebModel toRegisterResponseWebModel(RegisterCommandResponse response) {
        RegisterResponseWebModel webResponse = CommonHelper.copyProperties(response, new RegisterResponseWebModel());
        return webResponse;
    }

    public static LoginResponseWebModel toLoginResponseWebModel(LoginCommandResponse response) {
        LoginResponseWebModel webResponse = CommonHelper.copyProperties(response, new LoginResponseWebModel());
        webResponse.setUser(LoginResponseWebModel.UserInfo.builder()
                .email(response.getEmail())
                .name(response.getName())
                .build());
        return webResponse;
    }
}
