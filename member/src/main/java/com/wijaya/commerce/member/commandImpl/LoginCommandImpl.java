package com.wijaya.commerce.member.commandImpl;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.wijaya.commerce.member.command.LoginCommand;
import com.wijaya.commerce.member.commandImpl.model.LoginCommandRequest;
import com.wijaya.commerce.member.commandImpl.model.LoginCommandResponse;
import com.wijaya.commerce.member.modelDb.MemberModelDb;
import com.wijaya.commerce.member.modelDb.SessionManagerModelDb;
import com.wijaya.commerce.member.repository.MemberRepository;
import com.wijaya.commerce.member.repository.SessionManagerRepository;
import com.wijaya.commerce.member.serviceImpl.helper.JwtHelper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoginCommandImpl implements LoginCommand {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionManagerRepository sessionManagerRepository;
    private final JwtHelper jwtHelper;

    @Override
    public LoginCommandResponse doCommand(LoginCommandRequest request) {
        Optional<MemberModelDb> member = memberRepository.findByEmail(request.getEmail());
        if (member == null) {
            throw new RuntimeException("Member not found");
        }
        if (!passwordEncoder.matches(request.getPassword(), member.get().getPassword())) {
            throw new RuntimeException("Invalid password");
        }
        if (!member.get().getStatus().equals("ACTIVE")) {
            throw new RuntimeException("Member is not active");
        }
        String refreshToken = generateRefreshToken();
        SessionManagerModelDb session = createSession(member.get(), refreshToken);
        String accessToken = jwtHelper.generateAccessToken(member.get(), session.getId());

        session.setAccessToken(accessToken);
        sessionManagerRepository.save(session);

        member.get().setLastLoginAt(LocalDateTime.now());
        memberRepository.save(member.get());

        return toLoginCommandResponse(member.get(), session);
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
