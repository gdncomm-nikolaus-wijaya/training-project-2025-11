package com.wijaya.commerce.member.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.wijaya.commerce.member.modelDb.MemberModelDb;

public interface MemberRepository extends MongoRepository<MemberModelDb, String> {
    Optional<MemberModelDb> findByEmail(String email);

    Optional<MemberModelDb> findByPhoneNumber(String phoneNumber);
}
