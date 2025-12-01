package com.wijaya.commerce.member.web.controller;

import com.wijaya.commerce.member.commandImpl.model.LoginCommandRequest;
import com.wijaya.commerce.member.commandImpl.model.LoginCommandResponse;
import com.wijaya.commerce.member.commandImpl.model.RegisterCommandRequest;
import com.wijaya.commerce.member.commandImpl.model.RegisterCommandResponse;
import com.wijaya.commerce.member.restWebModel.request.LoginRequestWebModel;
import com.wijaya.commerce.member.restWebModel.request.RegisterRequestWebModel;
import com.wijaya.commerce.member.restWebModel.response.LoginResponseWebModel;
import com.wijaya.commerce.member.restWebModel.response.RegisterResponseWebModel;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.wijaya.commerce.member.command.CommandExecutor;
import com.wijaya.commerce.member.command.LoginCommand;
import com.wijaya.commerce.member.command.RegisterCommand;
import com.wijaya.commerce.member.constant.MemberApiPath;
import com.wijaya.commerce.member.restWebModel.response.WebResponse;
import com.wijaya.commerce.member.serviceImpl.helper.MemberResponseHelper;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class MemberController {
  private final CommandExecutor commandExecutor;

  @PostMapping(MemberApiPath.REGISTER)
  public WebResponse<RegisterResponseWebModel> register(@Valid @RequestBody RegisterRequestWebModel request) {
    RegisterCommandRequest commandRequest = RegisterCommandRequest.builder()
        .email(request.getEmail())
        .name(request.getName())
        .phoneNumber(request.getPhoneNumber())
        .password(request.getPassword())
        .build();
    RegisterCommandResponse response = commandExecutor.execute(RegisterCommand.class, commandRequest);

    return WebResponse.<RegisterResponseWebModel>builder().success(true)
        .data(MemberResponseHelper.toRegisterResponseWebModel(response)).build();
  }

  @PostMapping(MemberApiPath.LOGIN)
  public WebResponse<LoginResponseWebModel> login(@Valid @RequestBody LoginRequestWebModel request) {
    LoginCommandRequest commandRequest = LoginCommandRequest.builder()
        .email(request.getEmail())
        .password(request.getPassword())
        .build();
    LoginCommandResponse response = commandExecutor.execute(LoginCommand.class, commandRequest);

    return WebResponse.<LoginResponseWebModel>builder().success(true)
        .data(MemberResponseHelper.toLoginResponseWebModel(response)).build();
  }

}
