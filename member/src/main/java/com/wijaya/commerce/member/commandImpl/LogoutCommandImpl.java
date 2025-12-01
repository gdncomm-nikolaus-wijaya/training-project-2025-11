package com.wijaya.commerce.member.commandImpl;

import org.springframework.stereotype.Service;

import com.wijaya.commerce.member.command.LogoutCommand;
import com.wijaya.commerce.member.commandImpl.model.LogoutCommandRequest;
import com.wijaya.commerce.member.commandImpl.model.LogoutCommandResponse;
import com.wijaya.commerce.member.repository.SessionManagerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LogoutCommandImpl implements LogoutCommand {

    private final SessionManagerRepository sessionManagerRepository;

    @Override
    public LogoutCommandResponse doCommand(LogoutCommandRequest request) {
        String token = request.getAccessToken();
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        sessionManagerRepository.findByAccessToken(token).ifPresent(session -> {
            sessionManagerRepository.delete(session);
        });

        return LogoutCommandResponse.builder()
                .message("Logged out successfully")
                .build();
    }

}
