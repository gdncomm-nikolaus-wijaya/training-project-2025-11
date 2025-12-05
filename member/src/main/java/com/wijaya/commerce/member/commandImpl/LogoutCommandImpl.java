package com.wijaya.commerce.member.commandImpl;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.wijaya.commerce.member.command.LogoutCommand;
import com.wijaya.commerce.member.commandImpl.model.LogoutCommandRequest;
import com.wijaya.commerce.member.commandImpl.model.LogoutCommandResponse;
import com.wijaya.commerce.member.exception.InvalidTokenException;
import com.wijaya.commerce.member.modelDb.SessionManagerModelDb;
import com.wijaya.commerce.member.repository.SessionManagerRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogoutCommandImpl implements LogoutCommand {

    private final SessionManagerRepository sessionManagerRepository;

    @Override
    public LogoutCommandResponse doCommand(LogoutCommandRequest request) {
        try {
            String token = extractToken(request.getAccessToken());

            if (token == null || token.isEmpty()) {
                log.warn("Logout attempt with empty or null token");
                throw new InvalidTokenException("Invalid token");
            }

            Optional<SessionManagerModelDb> sessionOptional = sessionManagerRepository.findByAccessToken(token);

            if (sessionOptional.isEmpty()) {
                log.info("Logout attempt with non-existent or already expired session");
                throw new InvalidTokenException("Invalid token");
            }

            SessionManagerModelDb session = sessionOptional.get();
            String memberId = session.getMemberId();

            try {
                sessionManagerRepository.delete(session);
                log.info("Logout successful for member id: {}, session id: {}", memberId, session.getId());
            } catch (Exception e) {
                log.error("Failed to delete session for member id: {}, session id: {}", memberId, session.getId(), e);
                throw new RuntimeException("Failed to logout", e);
            }

            return buildResponse("Logged out successfully");

        } catch (InvalidTokenException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during logout", e);
            throw new RuntimeException("Logout failed due to server error", e);
        }
    }

    private String extractToken(String token) {
        if (token == null) {
            return null;
        }
        if (token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return token;
    }

    private LogoutCommandResponse buildResponse(String message) {
        return LogoutCommandResponse.builder()
                .message(message)
                .build();
    }

}
