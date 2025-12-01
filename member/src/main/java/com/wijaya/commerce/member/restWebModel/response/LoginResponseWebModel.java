package com.wijaya.commerce.member.restWebModel.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginResponseWebModel {

    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
    private UserInfo user;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class UserInfo {
        private String email;
        private String name;
    }
}
