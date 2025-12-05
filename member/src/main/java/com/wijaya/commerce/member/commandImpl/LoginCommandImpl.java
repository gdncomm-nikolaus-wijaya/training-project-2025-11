package com.wijaya.commerce.member.commandImpl;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.wijaya.commerce.member.command.LoginCommand;
import com.wijaya.commerce.member.commandImpl.model.LoginCommandRequest;
import com.wijaya.commerce.member.commandImpl.model.LoginCommandResponse;
import com.wijaya.commerce.member.exception.FailedLoginException;
import com.wijaya.commerce.member.exception.UserNotFoundException;
import com.wijaya.commerce.member.modelDb.MemberModelDb;
import com.wijaya.commerce.member.modelDb.SessionManagerModelDb;
import com.wijaya.commerce.member.repository.MemberRepository;
import com.wijaya.commerce.member.repository.SessionManagerRepository;
import com.wijaya.commerce.member.serviceImpl.helper.JwtHelper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginCommandImpl implements LoginCommand {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionManagerRepository sessionManagerRepository;
    private final JwtHelper jwtHelper;

    @Override
    public LoginCommandResponse doCommand(LoginCommandRequest request) {
        try {
            Optional<MemberModelDb> memberOptional = memberRepository.findByEmail(request.getEmail());
            if (memberOptional.isEmpty()) {
                log.warn("Login failed - user not found for email: {}", request.getEmail());
                throw new UserNotFoundException("Member not found");
            }

            MemberModelDb member = memberOptional.get();

            if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
                log.warn("Login failed - invalid password for email: {}", request.getEmail());
                throw new FailedLoginException("Invalid password");
            }
            if (!member.getStatus().equals("ACTIVE")) {
                log.warn("Login failed - inactive account for email: {}, status: {}", request.getEmail(),
                        member.getStatus());
                throw new FailedLoginException("Member is not active");
            }

            String refreshToken = generateRefreshToken();
            SessionManagerModelDb session = createSession(member, refreshToken);

            String accessToken;
            try {
                accessToken = jwtHelper.generateAccessToken(member, session.getId());
            } catch (Exception e) {
                log.error("Failed to generate JWT token for email: {}", request.getEmail(), e);
                throw new RuntimeException("Failed to generate access token", e);
            }

            try {
                session.setAccessToken(accessToken);
                member.setLastLoginAt(LocalDateTime.now());
                sessionManagerRepository.save(session);
                memberRepository.save(member);
            } catch (Exception e) {
                log.error("Failed to save session for email: {}", request.getEmail(), e);
                throw new RuntimeException("Failed to create session", e);
            }
            log.info("Login successful for email: {}, member id: {}", request.getEmail(), member.getId());
            return toLoginCommandResponse(member, session);
        } catch (UserNotFoundException e) {
            throw e;
        } catch (FailedLoginException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during login for email: {}", request.getEmail(), e);
            throw new RuntimeException("Login failed due to server error", e);
        }
    }

    private LoginCommandResponse toLoginCommandResponse(MemberModelDb member, SessionManagerModelDb session) {
        return LoginCommandResponse.builder()
                .email(member.getEmail())
                .name(member.getName())
                .phoneNumber(member.getPhoneNumber())
                .accessToken(session.getAccessToken())
                .refreshToken(session.getRefreshToken())
                .expiresIn(session.getAccessTokenExpiresAt())
                .createdAt(member.getCreatedAt())
                .updatedAt(member.getUpdatedAt())
                .build();
    }

    private String generateRefreshToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private SessionManagerModelDb createSession(MemberModelDb member, String refreshToken) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime accessTokenExpiresAt = now.plusNanos(jwtHelper.getJwtExpiration() * 1_000_000);
        LocalDateTime refreshTokenExpiresAt = now.plusDays(7);

        SessionManagerModelDb session = SessionManagerModelDb.builder()
                .memberId(member.getId())
                .refreshToken(refreshToken)
                .accessTokenExpiresAt(accessTokenExpiresAt)
                .refreshTokenExpiresAt(refreshTokenExpiresAt)
                .build();

        return sessionManagerRepository.save(session);
    }

}
