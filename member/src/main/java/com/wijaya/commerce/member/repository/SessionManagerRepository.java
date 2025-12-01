package com.wijaya.commerce.member.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.wijaya.commerce.member.modelDb.SessionManagerModelDb;

public interface SessionManagerRepository extends MongoRepository<SessionManagerModelDb, String> {
    Optional<SessionManagerModelDb> findByAccessToken(String accessToken);
}
