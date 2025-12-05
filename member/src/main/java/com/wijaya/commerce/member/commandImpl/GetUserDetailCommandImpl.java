package com.wijaya.commerce.member.commandImpl;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

import com.wijaya.commerce.member.command.GetUserDetailCommand;
import com.wijaya.commerce.member.commandImpl.model.GetUserDetailCommandRequest;
import com.wijaya.commerce.member.commandImpl.model.GetUserDetailCommandResponse;
import com.wijaya.commerce.member.exception.UserNotFoundException;
import com.wijaya.commerce.member.modelDb.MemberModelDb;
import com.wijaya.commerce.member.repository.MemberRepository;
import com.wijaya.commerce.member.service.helper.CacheHelper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetUserDetailCommandImpl implements GetUserDetailCommand {
    private final MemberRepository memberRepository;
    private final CacheHelper cacheService;

    @Override
    public GetUserDetailCommandResponse doCommand(GetUserDetailCommandRequest request) {
        try {
            GetUserDetailCommandResponse response = findUserById(request.getId());
            return response;
        } catch (UserNotFoundException e) {
            log.warn("User not found for id: {}", request.getId());
            throw e;
        } catch (Exception e) {
            log.error("Error executing GetUserDetailCommand for id: {}", request.getId(), e);
            throw e;
        }
    }

    private GetUserDetailCommandResponse findUserById(String id) {
        // Try to get from cache, but don't fail if cache is unavailable
        try {
            GetUserDetailCommandResponse cache = (GetUserDetailCommandResponse) cacheService.get(id);
            if (cache != null) {
                return cache;
            }
        } catch (Exception e) {
            log.warn("Failed to retrieve user from cache for id: {}, falling back to database", id, e);
        }

        // Fetch from database
        Optional<MemberModelDb> memberModelDbOptional = memberRepository.findById(id);
        if (memberModelDbOptional.isEmpty()) {
            throw new UserNotFoundException("User not found");
        }
        MemberModelDb memberModelDb = memberModelDbOptional.get();
        GetUserDetailCommandResponse response = GetUserDetailCommandResponse.builder()
                .id(memberModelDb.getId())
                .email(memberModelDb.getEmail())
                .phoneNumber(memberModelDb.getPhoneNumber())
                .name(memberModelDb.getName())
                .status(memberModelDb.getStatus())
                .createdAt(memberModelDb.getCreatedAt())
                .updatedAt(memberModelDb.getUpdatedAt())
                .lastLoginAt(memberModelDb.getLastLoginAt())
                .build();

        // Try to save to cache, but don't fail if cache is unavailable
        try {
            cacheService.set(id, response);
        } catch (Exception e) {
            log.warn("Failed to cache user detail for id: {}", id, e);
        }

        return response;
    }

}
