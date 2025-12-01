package com.wijaya.commerce.member.service.helper;

import org.springframework.stereotype.Service;

import com.wijaya.commerce.member.commandImpl.model.RegisterCommandResponse;
import com.wijaya.commerce.member.restWebModel.response.RegisterResponseWebModel;

@Service
public class MemberResponseHelper {
    public static RegisterResponseWebModel toRegisterResponseWebModel(RegisterCommandResponse response) {
        RegisterResponseWebModel webResponse = CommonHelper.copyProperties(response, new RegisterResponseWebModel());
        return webResponse;
    }
}
