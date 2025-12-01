package com.wijaya.commerce.member.commandImpl.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginCommandResponse {
    private String email;
    private String name;
    private String phoneNumber;
    private String accessToken;
    private String refreshToken;
    private LocalDateTime expiresIn;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
