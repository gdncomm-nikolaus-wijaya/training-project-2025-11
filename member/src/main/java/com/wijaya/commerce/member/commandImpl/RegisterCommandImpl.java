package com.wijaya.commerce.member.commandImpl;

import java.time.LocalDateTime;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.wijaya.commerce.member.command.RegisterCommand;
import com.wijaya.commerce.member.commandImpl.model.RegisterCommandRequest;
import com.wijaya.commerce.member.commandImpl.model.RegisterCommandResponse;
import com.wijaya.commerce.member.exception.EmailAlreadyExistsException;
import com.wijaya.commerce.member.exception.PhoneNumberAlreadyExistsException;
import com.wijaya.commerce.member.modelDb.MemberModelDb;
import com.wijaya.commerce.member.repository.MemberRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegisterCommandImpl implements RegisterCommand {

    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;

    @Override
    public RegisterCommandResponse doCommand(RegisterCommandRequest request) {
        Optional<MemberModelDb> account = memberRepository.findByEmail(request.getEmail());
        if (account.isPresent()) {
            log.warn("Registration failed - email already exists: {}", request.getEmail());
            throw new EmailAlreadyExistsException("Email already exists");
        }

        Optional<MemberModelDb> phoneNumber = memberRepository.findByPhoneNumber(request.getPhoneNumber());
        if (phoneNumber.isPresent()) {
            log.warn("Registration failed - phone number already exists: {}", request.getPhoneNumber());
            throw new PhoneNumberAlreadyExistsException("Phone number already exists");
        }

        // Encode password
        String passwordEncoded;
        try {
            passwordEncoded = passwordEncoder.encode(request.getPassword());
            log.debug("Password encoded successfully for email: {}", request.getEmail());
        } catch (Exception e) {
            log.error("Failed to encode password for email: {}", request.getEmail(), e);
            throw new RuntimeException("Failed to encode password", e);
        }

        // Build member model
        MemberModelDb memberModelDb = MemberModelDb.builder()
                .email(request.getEmail())
                .name(request.getName())
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoded)
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        try {
            memberRepository.save(memberModelDb);
        } catch (Exception e) {
            log.error("Failed to save member to database for email: {}", request.getEmail(), e);
            throw new RuntimeException("Failed to save member", e);
        }

        return toRegisterCommandResponse(memberModelDb);
    }

    private RegisterCommandResponse toRegisterCommandResponse(MemberModelDb memberModelDb) {
        return RegisterCommandResponse.builder()
                .email(memberModelDb.getEmail())
                .name(memberModelDb.getName())
                .phoneNumber(memberModelDb.getPhoneNumber())
                .createdAt(memberModelDb.getCreatedAt())
                .updatedAt(memberModelDb.getUpdatedAt())
                .build();
    }

}
